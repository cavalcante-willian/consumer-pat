package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErroEmailInvalidoTeste {

    @Test
    void deve_ser_da_categoria_validacao_com_o_codigo_da_constante() {
        // Preparar
        var erro = new ErroEmailInvalido();

        // Verificar
        assertThat(erro.categoria()).isEqualTo(CategoriaErro.VALIDACAO);
        assertThat(erro.codigo()).isEqualTo(ErroEmailInvalido.CODIGO);
    }

    @Test
    void deve_apontar_o_campo_email_quando_consultar_os_campos_com_erro() {
        // Verificar
        assertThat(new ErroEmailInvalido().camposComErro()).containsOnlyKeys("email");
    }

    @Test
    void deve_dizer_o_formato_esperado_quando_consultar_a_mensagem() {
        // Verificar
        assertThat(new ErroEmailInvalido().mensagem()).contains("nome@dominio.sufixo");
    }
}
