package com.algaworks.brewer.Service.Exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ServiceExceptionsTest {

    @ParameterizedTest
    @MethodSource("exceptionFactories")
    void shouldKeepProvidedMessage(Function<String, RuntimeException> factory) {
        RuntimeException exception = factory.apply("erro esperado");

        assertThat(exception).hasMessage("erro esperado");
    }

    private static Stream<Arguments> exceptionFactories() {
        return Stream.of(
                Arguments.of((Function<String, RuntimeException>) CpfCnpjClienteJaCadastradoException::new),
                Arguments.of((Function<String, RuntimeException>) ImpossivelExcluirEntidadeException::new),
                Arguments.of((Function<String, RuntimeException>) NomeCidadeJaCadastradaException::new),
                Arguments.of((Function<String, RuntimeException>) NomeEstiloJaCadastradoException::new),
                Arguments.of((Function<String, RuntimeException>) SenhaObrigatoriaUsuarioException::new),
                Arguments.of((Function<String, RuntimeException>) UsuarioJaCadastradoException::new));
    }
}
