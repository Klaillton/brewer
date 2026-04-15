package com.algaworks.brewer.storage.local;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class FotoStorageLocalTest {

    @Test
    void shouldReturnRelativeFotoUrl() {
        FotoStorageLocal storage = new FotoStorageLocal(Paths.get(System.getProperty("java.io.tmpdir"), "brewer-fotos-test"));

        assertThat(storage.getUrl("cerveja.png")).isEqualTo("/fotos/cerveja.png");
    }
}
