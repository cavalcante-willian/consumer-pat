package br.com.alelo.consumer.consumerpat.dominio.erros.conflito;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErroVersaoDivergenteTeste {

    private static final ErroVersaoDivergente ERRO = new ErroVersaoDivergente(new ConsumidorId(5),
            3);

    @Test
    void deve_ser_da_categoria_conflito_com_status_409() {
        // Verificar
        assertThat(ERRO.categoria()).isEqualTo(CategoriaErro.CONFLITO);
        assertThat(ERRO.categoria().statusHttp()).isEqualTo(409);
    }

    @Test
    void deve_expor_o_codigo_da_constante_quando_consultar_o_codigo() {
        // Verificar
        assertThat(ERRO.codigo()).isEqualTo(ErroVersaoDivergente.CODIGO);
    }

    @Test
    void deve_apontar_o_campo_versao_quando_consultar_os_campos_com_erro() {
        // Verificar
        assertThat(ERRO.camposComErro()).containsOnlyKeys("versao");
    }

    @Test
    void deve_dizer_o_consumidor_e_a_versao_e_pedir_para_reler_quando_consultar_a_mensagem() {
        // Verificar
        assertThat(ERRO.mensagem()).contains("5").contains("versão 3").contains("Releia");
    }
}
