package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErroDocumentoInvalidoTeste {

    @Test
    void deve_ser_da_categoria_validacao_com_o_codigo_da_constante() {
        // Preparar
        var erro = new ErroDocumentoInvalido();

        // Verificar
        assertThat(erro.categoria()).isEqualTo(CategoriaErro.VALIDACAO);
        assertThat(erro.codigo()).isEqualTo(ErroDocumentoInvalido.CODIGO);
    }

    @Test
    void deve_apontar_o_campo_documento_quando_consultar_os_campos_com_erro() {
        // Verificar
        assertThat(new ErroDocumentoInvalido().camposComErro()).containsOnlyKeys("documento");
    }

    @Test
    void deve_dizer_os_dois_tamanhos_aceitos_quando_consultar_a_mensagem() {
        // Verificar
        assertThat(new ErroDocumentoInvalido().mensagem()).contains("11").contains("14");
    }
}
