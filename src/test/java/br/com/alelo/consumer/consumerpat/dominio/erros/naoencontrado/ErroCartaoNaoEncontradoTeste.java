package br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado;

import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErroCartaoNaoEncontradoTeste {

    private static final NumeroDeCartao NUMERO = new NumeroDeCartao("5555000011112222");

    @Test
    void deve_ser_da_categoria_nao_encontrado_quando_consultar_a_categoria() {
        // Preparar
        var erro = new ErroCartaoNaoEncontrado(NUMERO, TipoCartao.FARMACIA);

        // Verificar
        assertThat(erro.categoria()).isEqualTo(CategoriaErro.NAO_ENCONTRADO);
        assertThat(erro.categoria().statusHttp()).isEqualTo(404);
    }

    @Test
    void deve_expor_o_codigo_da_constante_quando_consultar_o_codigo() {
        // Preparar
        var erro = new ErroCartaoNaoEncontrado(NUMERO, TipoCartao.FARMACIA);

        // Verificar
        assertThat(erro.codigo()).isEqualTo(ErroCartaoNaoEncontrado.CODIGO);
    }

    @Test
    void deve_dizer_o_tipo_e_o_numero_mascarado_quando_consultar_a_mensagem() {
        // Preparar
        var erro = new ErroCartaoNaoEncontrado(NUMERO, TipoCartao.COMBUSTIVEL);

        // Agir
        var mensagem = erro.mensagem();

        // Verificar
        assertThat(mensagem).contains("COMBUSTIVEL").contains("****2222");
    }

    @Test
    void deve_nunca_mostrar_o_numero_inteiro_quando_consultar_a_mensagem() {
        // Preparar
        var erro = new ErroCartaoNaoEncontrado(NUMERO, TipoCartao.ALIMENTACAO);

        // Verificar
        assertThat(erro.mensagem()).doesNotContain("5555000011112222");
        assertThat(erro.toString()).doesNotContain("5555000011112222");
    }

    @Test
    void deve_nao_apontar_campo_quando_consultar_os_campos_com_erro() {
        // Preparar
        var erro = new ErroCartaoNaoEncontrado(NUMERO, TipoCartao.FARMACIA);

        // Verificar
        assertThat(erro.camposComErro()).isEmpty();
    }
}
