package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErroTelefoneInvalidoTeste {

    @Test
    void deve_ser_da_categoria_validacao_com_o_codigo_da_constante() {
        // Preparar
        var erro = new ErroTelefoneInvalido("celular");

        // Verificar
        assertThat(erro.categoria()).isEqualTo(CategoriaErro.VALIDACAO);
        assertThat(erro.codigo()).isEqualTo(ErroTelefoneInvalido.CODIGO);
    }

    @Test
    void deve_apontar_o_campo_que_recebeu_quando_consultar_os_campos_com_erro() {
        // Verificar
        assertThat(new ErroTelefoneInvalido("telefoneResidencial").camposComErro())
                .containsOnlyKeys("telefoneResidencial");
    }

    @Test
    void deve_dizer_o_formato_esperado_quando_consultar_a_mensagem() {
        // Verificar
        assertThat(new ErroTelefoneInvalido("celular").mensagem()).contains("10 a 15 dígitos");
    }
}
