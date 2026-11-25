package br.com.alelo.consumer.consumerpat.api.comum.requisicao;

import br.com.alelo.consumer.consumerpat.aplicacao.fakes.SaidaDeLogFake;
import br.com.alelo.consumer.consumerpat.dominio.log.NivelDeLog;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FiltroDeLogDeConclusaoTeste {

    private final SaidaDeLogFake saida = new SaidaDeLogFake();
    private final FiltroDeLogDeConclusao filtro = new FiltroDeLogDeConclusao(saida);
    private final MockHttpServletRequest requisicao = new MockHttpServletRequest("POST",
            "/api/v1/compras");
    private final MockHttpServletResponse resposta = new MockHttpServletResponse();

    private static MockFilterChain cadeiaComStatus(int status) {
        return new MockFilterChain() {
            @Override
            public void doFilter(ServletRequest req, ServletResponse res) {
                ((MockHttpServletResponse) res).setStatus(status);
            }
        };
    }

    @Test
    void deve_registrar_a_conclusao_com_metodo_caminho_status_e_duracao() throws Exception {
        // Agir
        filtro.doFilter(requisicao, resposta, cadeiaComStatus(201));

        // Verificar
        assertThat(saida.eventos()).singleElement().satisfies(evento -> {
            assertThat(evento.evento()).isEqualTo("requisicao.concluida");
            assertThat(evento.nivel()).isEqualTo(NivelDeLog.INFORMACAO);
            assertThat(evento.valorDe("metodo")).contains("POST");
            assertThat(evento.valorDe("caminho")).contains("/api/v1/compras");
            assertThat(evento.valorDe("status")).contains(201);
            assertThat((Long) evento.valorDe("duracaoMs").orElseThrow()).isNotNegative();
        });
    }

    @Test
    void deve_registrar_a_duracao_medida_quando_a_requisicao_demora() throws Exception {
        // Preparar
        var lenta = new MockFilterChain() {
            @Override
            public void doFilter(ServletRequest req, ServletResponse res) {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        // Agir
        filtro.doFilter(requisicao, resposta, lenta);

        // Verificar
        assertThat((Long) saida.eventos().get(0).valorDe("duracaoMs").orElseThrow())
                .isGreaterThanOrEqualTo(25);
    }

    @Test
    void deve_registrar_em_erro_quando_o_servidor_falha() throws Exception {
        // Agir
        filtro.doFilter(requisicao, resposta, cadeiaComStatus(500));

        // Verificar
        assertThat(saida.eventos().get(0).nivel()).isEqualTo(NivelDeLog.ERRO);
    }

    @Test
    void deve_registrar_em_aviso_quando_ha_conflito() throws Exception {
        // Agir
        filtro.doFilter(requisicao, resposta, cadeiaComStatus(409));

        // Verificar
        assertThat(saida.eventos().get(0).nivel()).isEqualTo(NivelDeLog.AVISO);
    }

    @Test
    void deve_registrar_em_informacao_quando_o_resultado_de_negocio_e_esperado() throws Exception {
        // Agir: 422 é saldo insuficiente, e não falha do sistema
        filtro.doFilter(requisicao, resposta, cadeiaComStatus(422));

        // Verificar
        assertThat(saida.eventos().get(0).nivel()).isEqualTo(NivelDeLog.INFORMACAO);
    }

    @Test
    void deve_registrar_e_relancar_quando_a_requisicao_lanca() {
        // Preparar
        var quebrada = new MockFilterChain() {
            @Override
            public void doFilter(ServletRequest req, ServletResponse res) {
                throw new IllegalStateException("falhou");
            }
        };

        // Agir e Verificar: o evento sai mesmo assim, e a exceção segue para quem a trata
        assertThatThrownBy(() -> filtro.doFilter(requisicao, resposta, quebrada))
                .isInstanceOf(IllegalStateException.class);
        assertThat(saida.eventos()).hasSize(1);
    }

    @Test
    void deve_registrar_o_caminho_sem_a_query_string_quando_a_requisicao_tem_parametros()
            throws Exception {
        // Preparar: a query pode carregar dado do usuário
        requisicao.setQueryString("documento=12345678901");
        requisicao.setParameter("documento", "12345678901");

        // Agir
        filtro.doFilter(requisicao, resposta, cadeiaComStatus(200));

        // Verificar
        assertThat(saida.eventos().get(0).texto()).doesNotContain("12345678901")
                .doesNotContain("documento=");
    }
}
