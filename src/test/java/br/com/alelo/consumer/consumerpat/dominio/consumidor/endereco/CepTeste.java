package br.com.alelo.consumer.consumerpat.dominio.consumidor.endereco;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CepTeste {

    @Test
    void deve_aceitar_oito_digitos() {
        // Agir
        var cep = new Cep("01001000");

        // Verificar
        assertThat(cep.digitos()).isEqualTo("01001000");
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "0100100", "010010000", "01001-000", "0100100a" })
    @NullSource
    void deve_lancar_quando_nao_sao_oito_digitos(String digitos) {
        // Agir e Verificar
        assertThatThrownBy(() -> new Cep(digitos)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_guardar_o_zero_a_esquerda_quando_o_cep_comeca_com_zero() {
        // Verificar: por isso o CEP é texto, e não número
        assertThat(new Cep("01001000").digitos()).startsWith("0");
    }
}
