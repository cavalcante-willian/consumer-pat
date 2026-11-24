package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErroNumeroDeCartaoInvalidoTeste {

    @Test
    void deve_ser_da_categoria_validacao_quando_consultar_a_categoria() {
        // Preparar
        var erro = new ErroNumeroDeCartaoInvalido();

        // Verificar
        assertThat(erro.categoria()).isEqualTo(CategoriaErro.VALIDACAO);
        assertThat(erro.categoria().statusHttp()).isEqualTo(400);
    }

    @Test
    void deve_expor_o_codigo_da_constante_quando_consultar_o_codigo() {
        // Preparar
        var erro = new ErroNumeroDeCartaoInvalido();

        // Verificar
        assertThat(erro.codigo()).isEqualTo(ErroNumeroDeCartaoInvalido.CODIGO);
    }

    @Test
    void deve_apontar_o_campo_numero_cartao_quando_consultar_os_campos_com_erro() {
        // Preparar
        var erro = new ErroNumeroDeCartaoInvalido();

        // Verificar
        assertThat(erro.camposComErro()).containsOnlyKeys("numeroCartao");
        assertThat(erro.camposComErro().get("numeroCartao")).hasSize(1);
    }

    @Test
    void deve_dizer_o_formato_esperado_quando_consultar_a_mensagem() {
        // Preparar
        var erro = new ErroNumeroDeCartaoInvalido();

        // Verificar
        assertThat(erro.mensagem()).contains("13 a 19 dígitos");
    }
}
