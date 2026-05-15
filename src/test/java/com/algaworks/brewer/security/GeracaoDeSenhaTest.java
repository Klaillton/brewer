package com.algaworks.brewer.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GeracaoDeSenhaTest {

    @Test
    void shouldInstantiateUtilityClassWithoutErrors() {
        GeracaoDeSenha geracaoDeSenha = new GeracaoDeSenha();

        assertThat(geracaoDeSenha).isNotNull();
    }
}
