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
}
