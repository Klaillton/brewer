package com.algaworks.brewer.model.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.algaworks.brewer.model.Cliente;
import com.algaworks.brewer.model.TipoPessoa;
import com.algaworks.brewer.model.validation.group.CnpjGroup;
import com.algaworks.brewer.model.validation.group.CpfGroup;

class ClienteGroupSequenceProviderTest {

    private final ClienteGroupSequenceProvider provider = new ClienteGroupSequenceProvider();

    @Test
    void shouldReturnOnlyClienteClassWhenClienteIsNullOrHasNoTipoPessoa() {
        Cliente semTipoPessoa = new Cliente();

        List<Class<?>> gruposComNulo = provider.getValidationGroups(null);
        List<Class<?>> gruposSemTipo = provider.getValidationGroups(semTipoPessoa);

        assertThat(gruposComNulo).containsExactly(Cliente.class);
        assertThat(gruposSemTipo).containsExactly(Cliente.class);
    }

    @Test
    void shouldAppendGroupBasedOnTipoPessoa() {
        Cliente pessoaFisica = new Cliente();
        pessoaFisica.setTipoPessoa(TipoPessoa.FISICA);

        Cliente pessoaJuridica = new Cliente();
        pessoaJuridica.setTipoPessoa(TipoPessoa.JURIDICA);

        assertThat(provider.getValidationGroups(pessoaFisica)).containsExactly(Cliente.class, CpfGroup.class);
        assertThat(provider.getValidationGroups(pessoaJuridica)).containsExactly(Cliente.class, CnpjGroup.class);
    }
}
