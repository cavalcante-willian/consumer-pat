package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErroValorNaoPositivoTeste {

    @Test
    void deve_ser_da_categoria_validacao_quando_consultar_a_categoria() {
        // Preparar
        var erro = new ErroValorNaoPositivo();

        // Verificar
        assertThat(erro.categoria()).isEqualTo(CategoriaErro.VALIDACAO);
        assertThat(erro.categoria().statusHttp()).isEqualTo(400);
    }

    @Test
    void deve_expor_o_codigo_da_constante_quando_consultar_o_codigo() {
        // Preparar
        var erro = new ErroValorNaoPositivo();

        // Verificar
        assertThat(erro.codigo()).isEqualTo(ErroValorNaoPositivo.CODIGO);
    }

    @Test
    void deve_apontar_o_campo_valor_quando_consultar_os_campos_com_erro() {
        // Preparar
        var erro = new ErroValorNaoPositivo();

        // Verificar
        assertThat(erro.camposComErro()).containsOnlyKeys("valor");
        assertThat(erro.camposComErro().get("valor")).hasSize(1);
    }

    @Test
    void deve_pedir_quantia_maior_que_zero_quando_consultar_a_mensagem() {
        // Preparar
        var erro = new ErroValorNaoPositivo();

        // Verificar
        assertThat(erro.mensagem()).contains("maior que zero");
    }
}
