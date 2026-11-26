package br.com.alelo.consumer.consumerpat.medicao;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Fala HTTP com um sistema pelo cliente do próprio JDK, sem dependência nova.
 *
 * <p>
 * Usa HTTP/1.1, que é o que os dois sistemas falam, e um prazo por requisição, para um sistema
 * parado virar falha clara em vez de uma medição pendurada. É seguro entre threads, e é isso que
 * permite a disputa simultânea.
 */
public final class ClienteHttp {

    private static final Duration PRAZO_DA_CONEXAO = Duration.ofSeconds(5);

    private static final Duration PRAZO_DA_REQUISICAO = Duration.ofSeconds(120);

    private final HttpClient cliente = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(PRAZO_DA_CONEXAO).build();

    private final String base;

    /**
     * Cria o cliente de um sistema.
     *
     * @param base o endereço do sistema, sem barra no fim, como {@code http://localhost:8080}
     */
    public ClienteHttp(String base) {
        this.base = base;
    }

    /**
     * Faz um {@code GET}.
     *
     * @param caminho o caminho, com a query se houver, começando por barra
     * @return a resposta
     * @throws Exception se a requisição falha ou estoura o prazo
     */
    public RespostaHttp get(String caminho) throws Exception {
        return enviar(requisicao(caminho).GET().build());
    }

    /**
     * Faz um {@code POST} com corpo JSON.
     *
     * @param caminho o caminho, começando por barra
     * @param json    o corpo
     * @return a resposta
     * @throws Exception se a requisição falha ou estoura o prazo
     */
    public RespostaHttp post(String caminho, String json) throws Exception {
        return enviar(requisicao(caminho).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build());
    }

    private HttpRequest.Builder requisicao(String caminho) {
        return HttpRequest.newBuilder(URI.create(base + caminho)).timeout(PRAZO_DA_REQUISICAO);
    }

    private RespostaHttp enviar(HttpRequest requisicao) throws Exception {
        var resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofByteArray());
        return new RespostaHttp(resposta.statusCode(), resposta.body(),
                resposta.headers().firstValue("Location"));
    }
}
