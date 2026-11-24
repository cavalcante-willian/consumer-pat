package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErroValorMonetarioInvalidoTeste {

    @Test
    void deve_ser_da_categoria_validacao_quando_consultar_a_categoria() {
        // Preparar
        var erro = new ErroValorMonetarioInvalido();

        // Verificar
        assertThat(erro.categoria()).isEqualTo(CategoriaErro.VALIDACAO);
        assertThat(erro.categoria().statusHttp()).isEqualTo(400);
    }

    @Test
    void deve_expor_o_codigo_da_constante_quando_consultar_o_codigo() {
        // Preparar
        var erro = new ErroValorMonetarioInvalido();

        // Verificar
        assertThat(erro.codigo()).isEqualTo(ErroValorMonetarioInvalido.CODIGO);
    }

    @Test
    void deve_dizer_o_formato_esperado_quando_consultar_a_mensagem() {
        // Preparar
        var erro = new ErroValorMonetarioInvalido();

        // Verificar
        assertThat(erro.mensagem()).contains("13 dígitos inteiros").contains("6 casas decimais");
    }

    @Test
    void deve_nao_apontar_campos_quando_consultar_os_campos_com_erro() {
        // Preparar
        var erro = new ErroValorMonetarioInvalido();

        // Verificar
        assertThat(erro.camposComErro()).isEmpty();
    }
}
