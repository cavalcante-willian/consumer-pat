package br.com.alelo.consumer.consumerpat.medicao;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Um servidor HTTP de teste, do próprio JDK, que responde o que o teste manda e guarda o que
 * recebeu.
 *
 * <p>
 * Serve para provar o que o driver de medição e a bateria de segurança enviam: a rota, a query, o
 * corpo e os cabeçalhos de cada chamada, sem subir sistema nenhum. Escuta em porta livre, só na
 * própria máquina.
 */
public final class ServidorFalso implements AutoCloseable {

    /**
     * O que o servidor recebeu.
     *
     * @param metodo  o método HTTP
     * @param caminho o caminho com a query
     * @param corpo   o corpo, vazio num {@code GET}
     */
    public record Requisicao(String metodo, String caminho, String corpo) {
    }

    /**
     * O que o servidor devolve.
     *
     * @param status     o status
     * @param corpo      o corpo
     * @param cabecalhos os cabeçalhos
     */
    public record Resposta(int status, String corpo, Map<String, String> cabecalhos) {

        /**
         * Uma resposta sem cabeçalhos.
         *
         * @param status o status
         * @param corpo  o corpo
         * @return a resposta
         */
        public static Resposta de(int status, String corpo) {
            return new Resposta(status, corpo, Map.of());
        }
    }

    private final HttpServer servidor;

    private final List<Requisicao> recebidas = new CopyOnWriteArrayList<>();

    private volatile Map<String, String> cabecalhosDaUltima = Map.of();

    /**
     * Sobe o servidor.
     *
     * @param respondedor decide a resposta de cada requisição recebida
     * @throws IOException se não consegue abrir a porta
     */
    public ServidorFalso(Function<Requisicao, Resposta> respondedor) throws IOException {
        this.servidor = HttpServer
                .create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        servidor.createContext("/", troca -> {
            var corpo = new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            var requisicao = new Requisicao(troca.getRequestMethod(),
                    troca.getRequestURI().toString(), corpo);
            cabecalhosDaUltima = primeirosValores(troca.getRequestHeaders());
            recebidas.add(requisicao);
            var resposta = respondedor.apply(requisicao);
            var bytes = resposta.corpo().getBytes(StandardCharsets.UTF_8);
            resposta.cabecalhos()
                    .forEach((nome, valor) -> troca.getResponseHeaders().add(nome, valor));
            troca.sendResponseHeaders(resposta.status(), bytes.length);
            troca.getResponseBody().write(bytes);
            troca.close();
        });
        servidor.start();
    }

    private static Map<String, String> primeirosValores(Headers cabecalhos) {
        return cabecalhos.entrySet().stream().filter(cabecalho -> !cabecalho.getValue().isEmpty())
                .collect(Collectors.toMap(cabecalho -> cabecalho.getKey().toLowerCase(Locale.ROOT),
                        cabecalho -> cabecalho.getValue().get(0)));
    }

    /**
     * O endereço do servidor.
     *
     * @return {@code http://localhost:porta}
     */
    public String url() {
        return "http://localhost:" + servidor.getAddress().getPort();
    }

    /**
     * Tudo o que o servidor recebeu, na ordem.
     *
     * @return as requisições
     */
    public List<Requisicao> recebidas() {
        return recebidas;
    }

    /**
     * Um cabeçalho da última requisição recebida.
     *
     * @param nome o nome do cabeçalho, sem distinguir maiúsculas
     * @return o valor, ou vazio se a última requisição não o trouxe
     */
    public Optional<String> cabecalhoDaUltima(String nome) {
        return Optional.ofNullable(cabecalhosDaUltima.get(nome.toLowerCase(Locale.ROOT)));
    }

    @Override
    public void close() {
        servidor.stop(0);
    }
}
