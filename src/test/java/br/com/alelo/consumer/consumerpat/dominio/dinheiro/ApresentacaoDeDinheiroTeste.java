package br.com.alelo.consumer.consumerpat.dominio.dinheiro;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

class ApresentacaoDeDinheiroTeste {

    @ParameterizedTest
    @CsvSource({
            "29.985, HALF_UP, 29.99",
            "29.985, HALF_EVEN, 29.98",
            "29.997, HALF_UP, 30.00",
            "29.997, DOWN, 29.99",
            "10, HALF_EVEN, 10.00" })
    void deve_arredondar_como_o_modo_escolhido_quando_apresentar_em_centavos(
            String valor, RoundingMode modo, String esperado) {
        // Preparar
        var dinheiro = new Dinheiro(new BigDecimal(valor));

        // Agir
        var centavos = ApresentacaoDeDinheiro.emCentavos(dinheiro, modo);

        // Verificar
        assertThat(centavos).isEqualTo(new BigDecimal(esperado));
    }
}
