package com.algaworks.brewer.storage.local;

import static java.nio.file.FileSystems.getDefault;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.algaworks.brewer.storage.FotoStorage;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;

public class FotoStorageLocal implements FotoStorage {
	
	private static final Logger logger = LoggerFactory.getLogger(FotoStorageLocal.class);
	
	private Path local;
	private Path localTemporario;
	
	public FotoStorageLocal(){
		
		if(!System.getProperty("os.name").equals("Windows 10")){ //Verifica o SO
			this.local =(getDefault().getPath(System.getenv("HOME"), ".brewerfotos"));//Funciona na nuvem S3 da amazon, não funciona no windows
		} else {
			this.local =(getDefault().getPath(System.getenv("USERPROFILE"), ".brewerfotos"));//Funciona no windows
		}
		
		System.out.println("this.local: "+this.local);
		
		criarPastas();
	}
	
//	public FotoStorageLocal(Path path){//Passando o caminho no construtor
//		this.local = path;
//		
//		criarPastas();
//	}
	
	@Override
	public byte[] recuperarFotoTemporaria(String nome) {
		System.out.println("nome: "+nome);
		try {
			System.out.println("this.localTemporario.resolve(nome): "+this.localTemporario.resolve(nome));
			return Files.readAllBytes(this.localTemporario.resolve(nome));
		} catch (IOException e) {
			throw new RuntimeException("Erro lendo a foto temporária", e);
		}
	}
	
	@Override
	public String salvarTemporariamente(MultipartFile[] files) {
		String novoNome = null;
		if(files != null && files.length > 0){
			MultipartFile arquivo = files[0];
			novoNome = renomearArquivo(arquivo.getOriginalFilename());
			try {
				arquivo.transferTo(new File(this.localTemporario.toAbsolutePath().toString()+getDefault().getSeparator()+novoNome));
			} catch (IOException e) {
				throw new RuntimeException("Erro salvando a foto na pasta termporária", e);
			}
			
		}
		return novoNome;
	}
	
	@Override
	public void salvar(String foto) {
		try {
			Files.move(this.localTemporario.resolve(foto), this.local.resolve(foto));
		} catch (IOException e) {
			throw new RuntimeException("Erro movendo a foto para o destino final", e);
		}
		
		try {
			Thumbnails.of(this.local.resolve(foto).toString()).size(40, 68).toFiles(Rename.PREFIX_DOT_THUMBNAIL);
		} catch (IOException e) {
			throw new RuntimeException("Erro gerando thumbnail da foto");
		};
		
	}
	
	@Override
	public byte[] recuperar(String nome) {
		System.out.println("nome: "+nome);
		System.out.println("this.local: "+this.local);
		System.out.println("this.localTemporario: "+this.localTemporario);
		System.out.println("this.local.resolve(nome): "+this.local.resolve(nome));
		try {			
			return Files.readAllBytes(this.local.resolve(nome));
		} catch (IOException e) {
			throw new RuntimeException("Erro lendo a foto", e);
		}	
	}
	
	private void criarPastas(){		
		System.out.println("**********");
		System.out.println("criar pastas");
		System.out.println("**********");
		try {
			Files.createDirectories(this.local);	
			this.localTemporario = getDefault().getPath(this.local.toString(), "temp");
			System.out.println("localTemporario: "+this.localTemporario);
			Files.createDirectories(this.localTemporario);
			if(logger.isDebugEnabled()){
				logger.debug("Pastas criadas para salvar foto.");
				logger.debug("Pasta default: "+this.local.toAbsolutePath());
				logger.debug("Pasta temporaria: "+this.localTemporario.toAbsolutePath());
				
			}
		} catch (IOException e) {
			throw new RuntimeException("Erro criando pasta para salvar foto", e);
		}
	}
	
	private String renomearArquivo(String nomeOriginal){
		String novoNome = UUID.randomUUID().toString()+"-"+nomeOriginal;
		
		if(logger.isDebugEnabled()){
			logger.debug(String.format("Nome original: %s, novo nome: %s", nomeOriginal, novoNome));
		}
		
		return novoNome;
	}

	

	

}
