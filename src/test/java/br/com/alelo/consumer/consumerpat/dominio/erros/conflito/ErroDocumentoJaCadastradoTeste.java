package br.com.alelo.consumer.consumerpat.dominio.erros.conflito;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.documento.Documento;
import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErroDocumentoJaCadastradoTeste {

    private static final ErroDocumentoJaCadastrado ERRO = new ErroDocumentoJaCadastrado(
            new Documento("12345678901"));

    @Test
    void deve_ser_da_categoria_conflito_com_status_409() {
        // Verificar
        assertThat(ERRO.categoria()).isEqualTo(CategoriaErro.CONFLITO);
        assertThat(ERRO.categoria().statusHttp()).isEqualTo(409);
    }

    @Test
    void deve_expor_o_codigo_da_constante_quando_consultar_o_codigo() {
        // Verificar
        assertThat(ERRO.codigo()).isEqualTo(ErroDocumentoJaCadastrado.CODIGO);
    }

    @Test
    void deve_apontar_o_campo_documento_quando_consultar_os_campos_com_erro() {
        // Verificar
        assertThat(ERRO.camposComErro()).containsOnlyKeys("documento");
    }

    @Test
    void deve_nunca_mostrar_o_documento_inteiro_quando_consultar_a_mensagem() {
        // Verificar
        assertThat(ERRO.mensagem()).contains("****8901").doesNotContain("12345678901");
        assertThat(ERRO.toString()).doesNotContain("12345678901");
    }
}
