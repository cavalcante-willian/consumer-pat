package br.com.alelo.consumer.consumerpat.api.comum.seguranca;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FiltroDeCabecalhosDeSegurancaTeste {

    private final FiltroDeCabecalhosDeSeguranca filtro = new FiltroDeCabecalhosDeSeguranca();
    private final MockHttpServletRequest requisicao = new MockHttpServletRequest("GET",
            "/api/v1/consumidores");
    private final MockHttpServletResponse resposta = new MockHttpServletResponse();

    @Test
    void deve_escrever_os_cinco_cabecalhos_quando_a_requisicao_passa() throws Exception {
        // Agir
        filtro.doFilter(requisicao, resposta, new MockFilterChain());

        // Verificar
        CabecalhosDeSeguranca.PADRAO.forEach(
                (nome, valor) -> assertThat(resposta.getHeader(nome)).as(nome).isEqualTo(valor));
    }

    @Test
    void deve_escrever_os_cabecalhos_antes_de_a_requisicao_seguir() throws Exception {
        // Preparar
        var vistos = new ArrayList<String>();
        var cadeia = new MockFilterChain() {
            @Override
            public void doFilter(ServletRequest req, ServletResponse res) {
                vistos.add(((MockHttpServletResponse) res).getHeader("X-Frame-Options"));
            }
        };

        // Agir
        filtro.doFilter(requisicao, resposta, cadeia);

        // Verificar: o que vem depois já encontra os cabeçalhos escritos
        assertThat(vistos).containsExactly("DENY");
    }

    @Test
    void deve_manter_os_cabecalhos_quando_a_requisicao_lanca() {
        // Preparar
        var cadeia = new MockFilterChain() {
            @Override
            public void doFilter(ServletRequest req, ServletResponse res) {
                throw new IllegalStateException("falhou");
            }
        };

        // Agir e Verificar: a resposta de erro também sai protegida
        assertThatThrownBy(() -> filtro.doFilter(requisicao, resposta, cadeia))
                .isInstanceOf(IllegalStateException.class);
        assertThat(resposta.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
    }

    @Test
    void deve_substituir_o_valor_que_ja_existia_e_nao_acrescentar_um_segundo() throws Exception {
        // Preparar
        resposta.setHeader("Cache-Control", "max-age=3600");

        // Agir
        filtro.doFilter(requisicao, resposta, new MockFilterChain());

        // Verificar: um cabeçalho antigo não deixa o dado pessoal ser guardado
        assertThat(resposta.getHeaders("Cache-Control")).containsExactly("no-store");
    }

    @Test
    void deve_nao_escrever_cabecalho_nenhum_fora_do_contrato() throws Exception {
        // Agir
        filtro.doFilter(requisicao, resposta, new MockFilterChain());

        // Verificar: só os cinco, e nada que revele o servidor
        assertThat(resposta.getHeaderNames())
                .containsExactlyInAnyOrderElementsOf(CabecalhosDeSeguranca.PADRAO.keySet());
    }
}
