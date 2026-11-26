package br.com.alelo.consumer.consumerpat.seguranca;

import br.com.alelo.consumer.consumerpat.medicao.CartaoDeDisputa;
import br.com.alelo.consumer.consumerpat.medicao.ClienteHttp;
import br.com.alelo.consumer.consumerpat.medicao.CorposDoLegado;
import br.com.alelo.consumer.consumerpat.medicao.OperacoesDoAlvo;
import br.com.alelo.consumer.consumerpat.medicao.OperacoesDoLegado;
import br.com.alelo.consumer.consumerpat.medicao.RespostaHttp;
import com.jayway.jsonpath.JsonPath;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Faz as tentativas da bateria contra o legado, pelas rotas {@code /consumer/*}.
 *
 * <p>
 * Não depende de nenhuma classe do legado: conversa por HTTP com o sistema que estiver no endereço.
 * O legado ignora o que não conhece, e por isso a ordenação pedida vai na query e é descartada por
 * ele, que não tem ordenação.
 */
public final class SondaDoLegado implements SondaDeSeguranca {

    private static final int BASE_DA_BATERIA = 800_000_000;

    private static final String LISTAGEM = "/consumer/consumerList";

    private final ClienteHttp http;

    private final OperacoesDoLegado operacoes;

    /** Da JVM, e não da instância, pelo mesmo motivo da disputa: o banco é um só. */
    private static final AtomicInteger SEQUENCIA = new AtomicInteger();

    /**
     * Cria a sonda sobre o legado.
     *
     * @param http o cliente do endereço do legado
     */
    public SondaDoLegado(ClienteHttp http) {
        this.http = http;
        this.operacoes = new OperacoesDoLegado(http);
    }

    private static String codificar(String texto) {
        return URLEncoder.encode(texto, StandardCharsets.UTF_8);
    }

    @Override
    public String nome() {
        return "legado";
    }

    @Override
    public OperacoesDoAlvo operacoes() {
        return operacoes;
    }

    @Override
    public RespostaHttp atualizarMandandoSaldo(CartaoDeDisputa cartao, String saldo)
            throws Exception {
        List<Integer> ids = JsonPath.read(http.get(LISTAGEM).texto(),
                "$[?(@.drugstoreNumber==" + cartao.numero() + ")].id");
        return http.post("/consumer/updateConsumer", CorposDoLegado.atualizacaoComSaldo(ids.get(0),
                Integer.parseInt(cartao.numero()), saldo));
    }

    @Override
    public RespostaHttp comprarPorGet(CartaoDeDisputa cartao, String valor) throws Exception {
        return http.get("/consumer/buy?establishmentType=2&establishmentName=E&cardNumber="
                + cartao.numero() + "&productDescription=P&value=" + valor);
    }

    @Override
    public RespostaHttp cadastrarComNome(String nome) throws Exception {
        var documento = proximoDocumento();
        return http.post("/consumer/createConsumer",
                CorposDoLegado.cadastroComNome(nome, documento, documento + 1));
    }

    @Override
    public RespostaHttp cadastrarComCampoDesconhecido() throws Exception {
        return http.post("/consumer/createConsumer",
                "{\"name\":\"Z\",\"documentNumber\":" + proximoDocumento() + ",\"hacker\":\"1\"}");
    }

    @Override
    public RespostaHttp cadastrarComJsonMalformado() throws Exception {
        return http.post("/consumer/createConsumer", "{\"name\":");
    }

    @Override
    public RespostaHttp comprarComNumeroDeCartao(String numero) throws Exception {
        return http.get("/consumer/buy?establishmentType=2&establishmentName=E&cardNumber="
                + codificar(numero) + "&productDescription=P&value=1.0");
    }

    @Override
    public RespostaHttp listar(String ordenacao, String direcao) throws Exception {
        return http.get(
                LISTAGEM + "?ordenacao=" + codificar(ordenacao) + "&direcao=" + codificar(direcao));
    }

    @Override
    public RespostaHttp creditarCartaoInexistente() throws Exception {
        return http.get("/consumer/setcardbalance?cardNumber=987654321&value=1.0");
    }

    @Override
    public ExposicaoDeDados exporDadosDeConsumidor() throws Exception {
        var documento = proximoDocumento();
        http.post("/consumer/createConsumer",
                CorposDoLegado.cadastroComNome("Exposto", documento, documento + 1));
        return new ExposicaoDeDados(http.get(LISTAGEM).texto(), String.valueOf(documento),
                String.valueOf(documento + 1));
    }

    @Override
    public RespostaHttp requisicaoValida() throws Exception {
        return http.get(LISTAGEM);
    }

    @Override
    public RespostaHttp trace() throws Exception {
        return http.requisitar("TRACE", LISTAGEM, "", Map.of());
    }

    @Override
    public RespostaHttp preVooDeOrigemEstranha() throws Exception {
        return http.requisitar("OPTIONS", LISTAGEM, "", Map.of("Origin",
                "http://origem-estranha.exemplo", "Access-Control-Request-Method", "POST"));
    }

    @Override
    public RespostaHttp consoleDoH2() throws Exception {
        return http.get("/h2-console");
    }

    @Override
    public Optional<RespostaHttp> consoleDoH2DeFora() throws Exception {
        return PedidoPelaRede.get(http, "/h2-console/login.jsp");
    }

    @Override
    public RespostaHttp swagger() throws Exception {
        return http.get("/swagger-ui/index.html");
    }

    @Override
    public RespostaHttp especificacao() throws Exception {
        return http.get("/v3/api-docs");
    }

    private int proximoDocumento() {
        return BASE_DA_BATERIA + 2 * SEQUENCIA.incrementAndGet();
    }
}
