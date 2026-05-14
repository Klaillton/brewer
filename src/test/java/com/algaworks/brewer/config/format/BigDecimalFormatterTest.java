package com.algaworks.brewer.config.format;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Locale;

import org.junit.jupiter.api.Test;

class BigDecimalFormatterTest {

    @Test
    void shouldPrintAndParseUsingBrazilianPattern() throws ParseException {
        BigDecimalFormatter formatter = new BigDecimalFormatter("#,##0.00");

        String impresso = formatter.print(new BigDecimal("1234.50"), Locale.US);
        BigDecimal parseado = formatter.parse("1.234,50", Locale.US);

        assertThat(impresso).isEqualTo("1.234,50");
        assertThat(parseado).isEqualByComparingTo("1234.50");
    }
}
