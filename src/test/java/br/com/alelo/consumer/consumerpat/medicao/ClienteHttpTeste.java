package br.com.alelo.consumer.consumerpat.medicao;

import br.com.alelo.consumer.consumerpat.medicao.ServidorFalso.Requisicao;
import br.com.alelo.consumer.consumerpat.medicao.ServidorFalso.Resposta;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ClienteHttpTeste {

    @Test
    void deve_devolver_o_status_e_o_corpo_quando_fazer_get() throws Exception {
        try (var servidor = new ServidorFalso(r -> Resposta.de(200, "{\"ok\":true}"))) {
            // Agir
            var resposta = new ClienteHttp(servidor.url()).get("/consumer/consumerList?a=1");

            // Verificar
            assertThat(resposta.status()).isEqualTo(200);
            assertThat(resposta.texto()).isEqualTo("{\"ok\":true}");
            assertThat(resposta.tamanho()).isEqualTo(11);
            assertThat(servidor.recebidas())
                    .containsExactly(new Requisicao("GET", "/consumer/consumerList?a=1", ""));
        }
    }

    @Test
    void deve_enviar_o_corpo_como_json_quando_fazer_post() throws Exception {
        try (var servidor = new ServidorFalso(r -> Resposta.de(201, ""))) {
            // Agir
            new ClienteHttp(servidor.url()).post("/api/v1/consumidores", "{\"nome\":\"Ana\"}");

            // Verificar
            assertThat(servidor.recebidas()).containsExactly(
                    new Requisicao("POST", "/api/v1/consumidores", "{\"nome\":\"Ana\"}"));
        }
    }

    @Test
    void deve_devolver_o_location_quando_a_resposta_traz() throws Exception {
        try (var servidor = new ServidorFalso(
                r -> new Resposta(201, "", Map.of("Location", "/api/v1/consumidores/7")))) {
            // Agir
            var resposta = new ClienteHttp(servidor.url()).post("/api/v1/consumidores", "{}");

            // Verificar
            assertThat(resposta.localizacao()).contains("/api/v1/consumidores/7");
        }
    }

    @Test
    void deve_devolver_o_location_vazio_quando_a_resposta_nao_traz() throws Exception {
        try (var servidor = new ServidorFalso(r -> Resposta.de(404, ""))) {
            // Agir
            var resposta = new ClienteHttp(servidor.url()).get("/qualquer");

            // Verificar: o status de erro é dado, e não exceção
            assertThat(resposta.status()).isEqualTo(404);
            assertThat(resposta.localizacao()).isEmpty();
        }
    }

    @Test
    void deve_mandar_o_metodo_e_os_cabecalhos_escolhidos_quando_requisitar() throws Exception {
        try (var servidor = new ServidorFalso(r -> Resposta.de(204, ""))) {
            // Agir: um pré-voo de CORS, que o get e o post não alcançam
            new ClienteHttp(servidor.url()).requisitar("OPTIONS", "/api/v1/consumidores", "",
                    Map.of("Origin", "http://origem.exemplo", "Access-Control-Request-Method",
                            "POST"));

            // Verificar
            assertThat(servidor.recebidas())
                    .containsExactly(new Requisicao("OPTIONS", "/api/v1/consumidores", ""));
            assertThat(servidor.cabecalhoDaUltima("Origin")).contains("http://origem.exemplo");
            assertThat(servidor.cabecalhoDaUltima("access-control-request-method"))
                    .contains("POST");
        }
    }

    @Test
    void deve_mandar_o_corpo_junto_do_metodo_quando_o_metodo_aceita_corpo() throws Exception {
        try (var servidor = new ServidorFalso(r -> Resposta.de(200, ""))) {
            // Agir
            new ClienteHttp(servidor.url()).requisitar("PUT", "/x", "{\"a\":1}", Map.of());

            // Verificar
            assertThat(servidor.recebidas())
                    .containsExactly(new Requisicao("PUT", "/x", "{\"a\":1}"));
        }
    }

    @Test
    void deve_devolver_os_cabecalhos_da_resposta_com_o_nome_em_minusculas_quando_responder()
            throws Exception {
        try (var servidor = new ServidorFalso(r -> new Resposta(200, "",
                Map.of("X-Frame-Options", "DENY", "Cache-Control", "no-store")))) {
            // Agir
            var resposta = new ClienteHttp(servidor.url()).get("/qualquer");

            // Verificar: a busca não distingue maiúsculas, e o ausente é vazio
            assertThat(resposta.cabecalho("x-frame-options")).contains("DENY");
            assertThat(resposta.cabecalho("Cache-Control")).contains("no-store");
            assertThat(resposta.cabecalho("Content-Security-Policy")).isEmpty();
        }
    }
}
