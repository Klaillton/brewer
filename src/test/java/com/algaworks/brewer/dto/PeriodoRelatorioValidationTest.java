package com.algaworks.brewer.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class PeriodoRelatorioValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldRequireStartAndEndDates() {
        PeriodoRelatorio periodo = new PeriodoRelatorio();

        Set<ConstraintViolation<PeriodoRelatorio>> violations = validator.validate(periodo);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .contains("dataInicio", "dataFim");
    }
}
