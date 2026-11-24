package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErroTipoEstabelecimentoInvalidoTeste {

    @Test
    void deve_ser_da_categoria_validacao_quando_consultar_a_categoria() {
        // Preparar
        var erro = new ErroTipoEstabelecimentoInvalido(99);

        // Verificar
        assertThat(erro.categoria()).isEqualTo(CategoriaErro.VALIDACAO);
        assertThat(erro.categoria().statusHttp()).isEqualTo(400);
    }

    @Test
    void deve_expor_o_codigo_da_constante_quando_consultar_o_codigo() {
        // Preparar
        var erro = new ErroTipoEstabelecimentoInvalido(99);

        // Verificar
        assertThat(erro.codigo()).isEqualTo(ErroTipoEstabelecimentoInvalido.CODIGO);
    }

    @Test
    void deve_apontar_o_campo_tipo_estabelecimento_quando_consultar_os_campos_com_erro() {
        // Preparar
        var erro = new ErroTipoEstabelecimentoInvalido(99);

        // Verificar
        assertThat(erro.camposComErro()).containsOnlyKeys("tipoEstabelecimento");
        assertThat(erro.camposComErro().get("tipoEstabelecimento")).hasSize(1);
    }

    @Test
    void deve_dizer_que_o_tipo_nao_existe_quando_consultar_a_mensagem() {
        // Preparar
        var erro = new ErroTipoEstabelecimentoInvalido(99);

        // Verificar
        assertThat(erro.mensagem()).contains("não existe");
    }
}
