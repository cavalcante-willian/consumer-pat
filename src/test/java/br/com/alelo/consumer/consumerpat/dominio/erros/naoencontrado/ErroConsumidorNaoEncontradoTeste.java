package br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErroConsumidorNaoEncontradoTeste {

    private static final ErroConsumidorNaoEncontrado ERRO = new ErroConsumidorNaoEncontrado(
            new ConsumidorId(42));

    @Test
    void deve_ser_da_categoria_nao_encontrado_quando_consultar_a_categoria() {
        // Verificar
        assertThat(ERRO.categoria()).isEqualTo(CategoriaErro.NAO_ENCONTRADO);
        assertThat(ERRO.categoria().statusHttp()).isEqualTo(404);
    }

    @Test
    void deve_expor_o_codigo_da_constante_quando_consultar_o_codigo() {
        // Verificar
        assertThat(ERRO.codigo()).isEqualTo(ErroConsumidorNaoEncontrado.CODIGO);
    }

    @Test
    void deve_dizer_o_id_quando_consultar_a_mensagem() {
        // Verificar
        assertThat(ERRO.mensagem()).contains("42");
    }

    @Test
    void deve_nao_apontar_campo_quando_consultar_os_campos_com_erro() {
        // Verificar
        assertThat(ERRO.camposComErro()).isEmpty();
    }
}
