package br.com.alelo.consumer.consumerpat.dominio.consumidor.contato;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelefoneTeste {

    @ParameterizedTest
    @ValueSource(strings = { "1133334444", "11999998888", "551199999888877" })
    void deve_aceitar_de_dez_a_quinze_digitos(String digitos) {
        // Agir
        var telefone = new Telefone(digitos);

        // Verificar
        assertThat(telefone.digitos()).isEqualTo(digitos);
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "113333444", "5511999998888777", "11 99999-8888", "1199999888a" })
    @NullSource
    void deve_lancar_quando_nao_sao_de_dez_a_quinze_digitos(String digitos) {
        // Agir e Verificar
        assertThatThrownBy(() -> new Telefone(digitos))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_mascarar_o_texto_quando_imprimir_o_telefone() {
        // Agir
        var texto = new Telefone("11999998888").toString();

        // Verificar
        assertThat(texto).isEqualTo("****8888").doesNotContain("99999");
    }
}
