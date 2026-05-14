package com.algaworks.brewer.validation.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.algaworks.brewer.validation.AtributosConfirmacao;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class AtributoConfirmacaoValidatorTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldAcceptWhenBothFieldsAreNull() {
        SenhaForm form = new SenhaForm();

        Set<ConstraintViolation<SenhaForm>> violations = validator.validate(form);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldAcceptWhenFieldsMatch() {
        SenhaForm form = new SenhaForm();
        form.setSenha("segredo");
        form.setConfirmacaoSenha("segredo");

        Set<ConstraintViolation<SenhaForm>> violations = validator.validate(form);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectWhenFieldsDiffer() {
        SenhaForm form = new SenhaForm();
        form.setSenha("segredo");
        form.setConfirmacaoSenha("outro");

        Set<ConstraintViolation<SenhaForm>> violations = validator.validate(form);

        assertThat(violations).hasSize(1);
        ConstraintViolation<SenhaForm> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("confirmacaoSenha");
        assertThat(violation.getMessage()).isEqualTo("Confirmação inválida");
    }

    @AtributosConfirmacao(atributo = "senha", atributoConfirmacao = "confirmacaoSenha", message = "Confirmação inválida")
    public static class SenhaForm {
        private String senha;
        private String confirmacaoSenha;

        public String getSenha() {
            return senha;
        }

        public void setSenha(String senha) {
            this.senha = senha;
        }

        public String getConfirmacaoSenha() {
            return confirmacaoSenha;
        }

        public void setConfirmacaoSenha(String confirmacaoSenha) {
            this.confirmacaoSenha = confirmacaoSenha;
        }
    }
}
