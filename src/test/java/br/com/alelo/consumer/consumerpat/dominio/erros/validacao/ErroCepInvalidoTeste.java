package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErroCepInvalidoTeste {

    @Test
    void deve_ser_da_categoria_validacao_com_o_codigo_da_constante() {
        // Preparar
        var erro = new ErroCepInvalido();

        // Verificar
        assertThat(erro.categoria()).isEqualTo(CategoriaErro.VALIDACAO);
        assertThat(erro.codigo()).isEqualTo(ErroCepInvalido.CODIGO);
    }

    @Test
    void deve_apontar_o_campo_cep_quando_consultar_os_campos_com_erro() {
        // Verificar
        assertThat(new ErroCepInvalido().camposComErro()).containsOnlyKeys("cep");
    }

    @Test
    void deve_dizer_a_quantidade_de_digitos_quando_consultar_a_mensagem() {
        // Verificar
        assertThat(new ErroCepInvalido().mensagem()).contains("8 dígitos");
    }
}
