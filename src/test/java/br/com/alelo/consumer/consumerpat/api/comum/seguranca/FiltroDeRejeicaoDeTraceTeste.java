package br.com.alelo.consumer.consumerpat.api.comum.seguranca;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class FiltroDeRejeicaoDeTraceTeste {

    private final FiltroDeRejeicaoDeTrace filtro = new FiltroDeRejeicaoDeTrace();

    @Test
    void deve_agir_so_no_trace_quando_olhar_o_metodo() {
        // Agir e Verificar: o filtro é dispensado em todo método que não é TRACE
        assertThat(filtro.shouldNotFilter(new MockHttpServletRequest("TRACE", "/x"))).isFalse();
        assertThat(filtro.shouldNotFilter(new MockHttpServletRequest("GET", "/x"))).isTrue();
        assertThat(filtro.shouldNotFilter(new MockHttpServletRequest("POST", "/x"))).isTrue();
        assertThat(filtro.shouldNotFilter(new MockHttpServletRequest("OPTIONS", "/x"))).isTrue();
    }

    @Test
    void deve_reconhecer_o_trace_sem_distinguir_maiusculas_quando_olhar_o_metodo() {
        // Agir e Verificar
        assertThat(filtro.shouldNotFilter(new MockHttpServletRequest("trace", "/x"))).isFalse();
    }

    @Test
    void deve_valer_tambem_para_o_despacho_de_erro_quando_olhar_o_despacho() {
        // Agir e Verificar: é na página de erro que o eco sai, e um filtro comum a ignoraria
        assertThat(filtro.shouldNotFilterErrorDispatch()).isFalse();
    }

    @Test
    void deve_responder_405_sem_corpo_e_nao_deixar_a_requisicao_seguir_quando_for_trace()
            throws Exception {
        // Preparar
        var requisicao = new MockHttpServletRequest("TRACE", "/api/v1/consumidores");
        requisicao.addHeader("Cookie", "sessao=segredo");
        var resposta = new MockHttpServletResponse();
        var cadeia = new MockFilterChain();

        // Agir
        filtro.doFilterInternal(requisicao, resposta, cadeia);

        // Verificar: nada do que o cliente mandou volta, e a cadeia nem é chamada
        assertThat(resposta.getStatus()).isEqualTo(405);
        assertThat(resposta.getContentAsString()).isEmpty();
        assertThat(cadeia.getRequest()).isNull();
    }
}
