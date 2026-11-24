package br.com.alelo.consumer.consumerpat.dominio.texto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TextoLimitadoTeste {

    @Test
    void deve_devolver_o_texto_sem_espaco_nas_pontas_quando_cabe_no_limite() {
        // Agir
        var texto = TextoLimitado.exigir("  Maria  ", "O nome", 5);

        // Verificar
        assertThat(texto).isEqualTo("Maria");
    }

    @Test
    void deve_aceitar_o_texto_do_tamanho_exato_do_limite() {
        // Agir
        var texto = TextoLimitado.exigir("12345", "O nome", 5);

        // Verificar
        assertThat(texto).isEqualTo("12345");
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "   ", "123456" })
    @NullSource
    void deve_lancar_quando_o_texto_e_vazio_nulo_ou_maior_que_o_limite(String texto) {
        // Agir e Verificar
        assertThatThrownBy(() -> TextoLimitado.exigir(texto, "O nome", 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O nome precisa ter de 1 a 5 caracteres");
    }

    @Test
    void deve_contar_o_tamanho_depois_de_tirar_o_espaco_quando_o_texto_tem_espaco_nas_pontas() {
        // Agir
        var texto = TextoLimitado.exigir("   12345   ", "O nome", 5);

        // Verificar
        assertThat(texto).isEqualTo("12345");
    }

    @Test
    void deve_nunca_levar_o_valor_na_mensagem_quando_lancar() {
        // Agir e Verificar
        assertThatThrownBy(() -> TextoLimitado.exigir("segredo-demais", "O nome", 5))
                .hasMessageNotContaining("segredo");
    }
}
