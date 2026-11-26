package br.com.alelo.consumer.consumerpat.medicao;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * Um cliente do mesmo sistema, alcançado por outro endereço.
     *
     * <p>
     * Mantém o esquema e a porta e troca o endereço. Serve para pedir a um sistema que roda na
     * própria máquina pelo endereço de uma interface de rede, e não por {@code localhost}: o
     * sistema passa a ver uma origem que não é loopback, e é assim que se vê o que ele faz com quem
     * vem de fora.
     *
     * @param endereco o endereço novo, sem esquema nem porta
     * @return um cliente sobre o mesmo esquema e a mesma porta
     */
    public ClienteHttp noEndereco(String endereco) {
        var atual = URI.create(base);
        return new ClienteHttp(atual.getScheme() + "://" + endereco + ":" + atual.getPort());
    }

    /**
     * Faz um {@code GET}.
     *
     * @param caminho o caminho, com a query se houver, começando por barra
     * @return a resposta
     * @throws Exception se a requisição falha ou estoura o prazo
     */
    public RespostaHttp get(String caminho) throws Exception {
        return requisitar("GET", caminho, "", Map.of());
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
        return requisitar("POST", caminho, json, Map.of("Content-Type", "application/json"));
    }

    /**
     * Faz uma requisição de qualquer método, com os cabeçalhos que o chamador escolher.
     *
     * <p>
     * Serve para o que o {@link #get} e o {@link #post} não alcançam: {@code TRACE},
     * {@code OPTIONS} e um pré-voo de CORS com {@code Origin}.
     *
     * @param metodo     o método HTTP
     * @param caminho    o caminho, começando por barra
     * @param corpo      o corpo, ou vazio para não mandar nenhum
     * @param cabecalhos os cabeçalhos a mandar
     * @return a resposta, qualquer que seja o status
     * @throws Exception se a requisição falha ou estoura o prazo
     */
    public RespostaHttp requisitar(String metodo, String caminho, String corpo,
            Map<String, String> cabecalhos) throws Exception {
        var construtor = HttpRequest.newBuilder(URI.create(base + caminho))
                .timeout(PRAZO_DA_REQUISICAO).method(metodo,
                        Optional.of(corpo).filter(texto -> !texto.isEmpty())
                                .map(HttpRequest.BodyPublishers::ofString)
                                .orElseGet(HttpRequest.BodyPublishers::noBody));
        cabecalhos.forEach(construtor::header);
        var resposta = cliente.send(construtor.build(), HttpResponse.BodyHandlers.ofByteArray());
        return new RespostaHttp(resposta.statusCode(), resposta.body(),
                resposta.headers().firstValue("Location"), primeirosValores(resposta));
    }

    private static Map<String, String> primeirosValores(HttpResponse<byte[]> resposta) {
        return resposta.headers().map().entrySet().stream()
                .filter(cabecalho -> !cabecalho.getValue().isEmpty())
                .collect(Collectors.toMap(cabecalho -> cabecalho.getKey().toLowerCase(Locale.ROOT),
                        cabecalho -> cabecalho.getValue().get(0), (primeiro, segundo) -> primeiro));
    }
}
