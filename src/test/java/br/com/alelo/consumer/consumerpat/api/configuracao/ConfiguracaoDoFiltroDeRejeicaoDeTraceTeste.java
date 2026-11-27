package br.com.alelo.consumer.consumerpat.api.configuracao;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.DispatcherType;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

class ConfiguracaoDoFiltroDeRejeicaoDeTraceTeste {

    @Test
    void deve_registrar_na_posicao_da_recusa_do_trace_quando_ler_a_ordem() {
        // Agir
        var ordem = new ConfiguracaoDoFiltroDeRejeicaoDeTrace().filtroDeRejeicaoDeTrace()
                .getOrder();

        // Verificar
        assertThat(ordem).isEqualTo(OrdemDosFiltros.REJEICAO_DE_TRACE);
    }

    @Test
    void deve_aplicar_a_toda_rota_e_tambem_ao_despacho_de_erro_quando_ler_o_registro() {
        // Agir
        var registro = new ConfiguracaoDoFiltroDeRejeicaoDeTrace().filtroDeRejeicaoDeTrace();

        // Verificar: o eco sai na página de erro, e não só na requisição original
        assertThat(registro.getUrlPatterns()).containsExactly("/*");
        assertThat(ReflectionTestUtils.getField(registro, "dispatcherTypes"))
                .isEqualTo(EnumSet.of(DispatcherType.REQUEST, DispatcherType.ERROR));
    }
}
