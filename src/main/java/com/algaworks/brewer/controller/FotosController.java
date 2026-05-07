package com.algaworks.brewer.controller;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import com.algaworks.brewer.dto.FotoDTO;
import com.algaworks.brewer.storage.FotoStorage;
import com.algaworks.brewer.storage.FotoStorageRunnable;

@RestController
@RequestMapping("/fotos")
public class FotosController {

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
			"image/jpeg", "image/png", "image/gif", "image/webp");

	@Autowired
	private FotoStorage fotoStorage;

	@PostMapping
	public DeferredResult<FotoDTO> upload(@RequestParam("files[]") MultipartFile[] files){
		
		DeferredResult<FotoDTO> resultado = new DeferredResult<>();

		if (files == null || files.length == 0) {
			resultado.setErrorResult(new IllegalArgumentException("Nenhum arquivo enviado"));
			return resultado;
		}

		String contentType = files[0].getContentType();
		if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
			resultado.setErrorResult(new IllegalArgumentException(
					"Tipo de arquivo nao permitido. Envie somente imagens (JPEG, PNG, GIF ou WebP)."));
			return resultado;
		}

		Thread thread = new Thread(new FotoStorageRunnable(files, resultado, fotoStorage));
		thread.start();
		
		return resultado;
	
	}
	
	
	@GetMapping("/{nome:.*}")
	public ResponseEntity<byte[]> recuperar(@PathVariable String nome) {
		MediaType mediaType = MediaTypeFactory.getMediaType(nome).orElse(MediaType.APPLICATION_OCTET_STREAM);
		return ResponseEntity.ok()
				.contentType(mediaType)
				.body(fotoStorage.recuperar(nome));
	}
	
}
