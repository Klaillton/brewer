package com.algaworks.brewer.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import com.algaworks.brewer.dto.FotoDTO;

public class FotoStorageRunnable implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(FotoStorageRunnable.class);

	private MultipartFile[] files;
	private DeferredResult<FotoDTO> resultado;
	private FotoStorage fotoStorage;
	
	public FotoStorageRunnable(MultipartFile[] files, DeferredResult<FotoDTO> resultado, FotoStorage fotoStorage){
		this.files = files;
		this.resultado = resultado;
		this.fotoStorage = fotoStorage;
	}

	@Override
	public void run() {
		try {
			String nomeFoto = this.fotoStorage.salvar(files);
			String contentType = files[0].getContentType();
			resultado.setResult(new FotoDTO(nomeFoto, contentType, fotoStorage.getUrl(nomeFoto)));
		} catch (Exception e) {
			logger.error("Erro ao salvar foto: {}", e.getMessage(), e);
			resultado.setErrorResult(e);
		}
	}

}
