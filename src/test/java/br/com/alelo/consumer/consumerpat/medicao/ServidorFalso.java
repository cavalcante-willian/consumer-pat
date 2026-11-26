package br.com.alelo.consumer.consumerpat.medicao;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * Um servidor HTTP de teste, do próprio JDK, que responde o que o teste manda e guarda o que
 * recebeu.
 *
 * <p>
 * Serve para provar o que o driver de medição envia: a rota, a query e o corpo de cada operação,
 * sem subir sistema nenhum. Escuta em porta livre, só na própria máquina.
 */
final class ServidorFalso implements AutoCloseable {

    /**
     * O que o servidor recebeu.
     *
     * @param metodo  o método HTTP
     * @param caminho o caminho com a query
     * @param corpo   o corpo, vazio num {@code GET}
     */
    record Requisicao(String metodo, String caminho, String corpo) {
    }

    /**
     * O que o servidor devolve.
     *
     * @param status     o status
     * @param corpo      o corpo
     * @param cabecalhos os cabeçalhos
     */
    record Resposta(int status, String corpo, Map<String, String> cabecalhos) {

        static Resposta de(int status, String corpo) {
            return new Resposta(status, corpo, Map.of());
        }
    }

    private final HttpServer servidor;

    private final List<Requisicao> recebidas = new CopyOnWriteArrayList<>();

    ServidorFalso(Function<Requisicao, Resposta> respondedor) throws IOException {
        this.servidor = HttpServer
                .create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        servidor.createContext("/", troca -> {
            var corpo = new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            var requisicao = new Requisicao(troca.getRequestMethod(),
                    troca.getRequestURI().toString(), corpo);
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

    String url() {
        return "http://localhost:" + servidor.getAddress().getPort();
    }

    List<Requisicao> recebidas() {
        return recebidas;
    }

    @Override
    public void close() {
        servidor.stop(0);
    }
}
