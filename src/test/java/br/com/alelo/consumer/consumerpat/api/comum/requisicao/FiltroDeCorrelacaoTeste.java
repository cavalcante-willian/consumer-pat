package br.com.alelo.consumer.consumerpat.api.comum.requisicao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FiltroDeCorrelacaoTeste {

    private static final String UUID_DO_CLIENTE = "0f1c9c1e-8a45-4d5e-9f77-1a2b3c4d5e6f";

    private final FiltroDeCorrelacao filtro = new FiltroDeCorrelacao();
    private final MockHttpServletRequest requisicao = new MockHttpServletRequest("GET",
            "/api/v1/consumidores");
    private final MockHttpServletResponse resposta = new MockHttpServletResponse();

    @AfterEach
    void limpar() {
        MDC.clear();
    }

    @Test
    void deve_devolver_o_id_de_requisicao_quando_qualquer_resposta() throws Exception {
        // Agir
        filtro.doFilter(requisicao, resposta, new MockFilterChain());

        // Verificar: o id volta em toda resposta, não só nas de erro
        assertThat(resposta.getHeader("X-Request-Id")).hasSize(36);
    }

    @Test
    void deve_aceitar_o_id_do_cliente_quando_ele_e_um_uuid() throws Exception {
        // Preparar
        requisicao.addHeader("X-Request-Id", UUID_DO_CLIENTE);

        // Agir
        filtro.doFilter(requisicao, resposta, new MockFilterChain());

        // Verificar
        assertThat(resposta.getHeader("X-Request-Id")).isEqualTo(UUID_DO_CLIENTE);
    }

    @Test
    void deve_regerar_o_id_quando_o_do_cliente_nao_e_um_uuid() throws Exception {
        // Preparar
        requisicao.addHeader("X-Request-Id", "forjado\nINFO falso");

        // Agir
        filtro.doFilter(requisicao, resposta, new MockFilterChain());

        // Verificar
        assertThat(resposta.getHeader("X-Request-Id")).hasSize(36).doesNotContain("forjado");
    }

    @Test
    void deve_ter_o_id_no_mdc_durante_a_requisicao_quando_o_filtro_abre_a_correlacao()
            throws Exception {
        // Preparar
        var visto = new AtomicReference<String>();
        var cadeia = new MockFilterChain() {
            @Override
            public void doFilter(javax.servlet.ServletRequest req,
                    javax.servlet.ServletResponse res) throws IOException, ServletException {
                visto.set(MDC.get("idRequisicao"));
                super.doFilter(req, res);
            }
        };
        requisicao.addHeader("X-Request-Id", UUID_DO_CLIENTE);

        // Agir
        filtro.doFilter(requisicao, resposta, cadeia);

        // Verificar: o código de baixo vê o id, e o IdDaRequisicaoAtual o lê
        assertThat(visto.get()).isEqualTo(UUID_DO_CLIENTE);
    }

    @Test
    void deve_limpar_o_mdc_quando_a_requisicao_termina() throws Exception {
        // Agir
        filtro.doFilter(requisicao, resposta, new MockFilterChain());

        // Verificar: nada vaza para a requisição seguinte, na mesma thread do pool
        assertThat(MDC.get("idRequisicao")).isNull();
        assertThat(IdDaRequisicaoAtual.ler()).isNull();
    }

    @Test
    void deve_limpar_o_mdc_quando_a_requisicao_lanca() {
        // Preparar
        var cadeia = new MockFilterChain() {
            @Override
            public void doFilter(javax.servlet.ServletRequest req,
                    javax.servlet.ServletResponse res) {
                throw new IllegalStateException("falhou");
            }
        };

        // Agir e Verificar
        assertThatThrownBy(() -> filtro.doFilter(requisicao, resposta, cadeia))
                .isInstanceOf(IllegalStateException.class);
        assertThat(MDC.get("idRequisicao")).isNull();
    }

    @Test
    void deve_preservar_o_que_ja_estava_no_mdc_quando_remover_so_o_proprio_id() throws Exception {
        // Preparar
        MDC.put("outraChave", "valor");

        // Agir
        filtro.doFilter(requisicao, resposta, new MockFilterChain());

        // Verificar
        assertThat(MDC.get("outraChave")).isEqualTo("valor");
    }

    @Test
    void deve_dar_ids_diferentes_quando_duas_requisicoes_seguidas_nao_mandam_cabecalho()
            throws Exception {
        // Agir
        filtro.doFilter(requisicao, resposta, new MockFilterChain());
        var segunda = new MockHttpServletResponse();
        filtro.doFilter(new MockHttpServletRequest("GET", "/x"), segunda, new MockFilterChain());

        // Verificar
        assertThat(resposta.getHeader("X-Request-Id"))
                .isNotEqualTo(segunda.getHeader("X-Request-Id"));
    }
}
