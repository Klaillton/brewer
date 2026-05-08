package com.algaworks.brewer.repository.listener;

import com.algaworks.brewer.config.ApplicationContextProvider;
import com.algaworks.brewer.model.Cerveja;
import com.algaworks.brewer.storage.FotoStorage;

import jakarta.persistence.PostLoad;

public class CervejaEntityListener {

	@PostLoad
	public void postLoad(final Cerveja cerveja) {
		FotoStorage fotoStorage = ApplicationContextProvider.getBean(FotoStorage.class);
		cerveja.setUrlFoto(fotoStorage.getUrl(cerveja.getFotoOuMock()));
		cerveja.setUrlThumbnailFoto(fotoStorage.getUrl(FotoStorage.THUMBNAIL_PREFIX + cerveja.getFotoOuMock()));
	}

}
