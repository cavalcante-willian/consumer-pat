package br.com.alelo.consumer.consumerpat.dominio.consumidor.documento;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentoTeste {

    @ParameterizedTest
    @ValueSource(strings = { "12345678901", "12345678000195" })
    void deve_aceitar_onze_ou_catorze_digitos(String digitos) {
        // Agir
        var documento = new Documento(digitos);

        // Verificar
        assertThat(documento.digitos()).isEqualTo(digitos);
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "1234567890", "123456789012", "1234567890123", "123456780001951",
            "1234567890a", "123.456.789-01" })
    @NullSource
    void deve_lancar_quando_nao_sao_onze_nem_catorze_digitos(String digitos) {
        // Agir e Verificar
        assertThatThrownBy(() -> new Documento(digitos))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_mascarar_o_texto_quando_imprimir_o_documento() {
        // Agir
        var texto = new Documento("12345678901").toString();

        // Verificar
        assertThat(texto).isEqualTo("****8901").doesNotContain("1234567");
    }

    @Test
    void deve_nunca_levar_o_valor_na_mensagem_quando_lancar() {
        // Agir e Verificar
        assertThatThrownBy(() -> new Documento("123456")).hasMessageNotContaining("123456");
    }

    @Test
    void deve_ser_igual_quando_os_digitos_sao_os_mesmos() {
        // Verificar
        assertThat(new Documento("12345678901")).isEqualTo(new Documento("12345678901"));
    }
}
