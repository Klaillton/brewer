package com.algaworks.brewer.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

class FotoStorageDefaultMethodTest {

    @Test
    void shouldRenameFileWithUuidPrefix() {
        FotoStorage storage = new FakeFotoStorage();

        String renomeado = storage.renomearArquivo("cerveja.png");

        assertThat(renomeado).endsWith("_cerveja.png");
        String uuid = renomeado.substring(0, renomeado.indexOf('_'));
        assertThat(UUID.fromString(uuid)).isNotNull();
    }

    private static class FakeFotoStorage implements FotoStorage {
        @Override
        public String salvar(MultipartFile[] files) {
            return null;
        }

        @Override
        public byte[] recuperar(String nome) {
            return new byte[0];
        }

        @Override
        public byte[] recuperarThumbnail(String fotoCerveja) {
            return new byte[0];
        }

        @Override
        public void excluir(String foto) {
        }

        @Override
        public String getUrl(String foto) {
            return null;
        }
    }
}
