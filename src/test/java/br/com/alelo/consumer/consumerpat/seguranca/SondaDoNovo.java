package br.com.alelo.consumer.consumerpat.seguranca;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.fabricas.CorposDaApi;
import br.com.alelo.consumer.consumerpat.fabricas.DocumentosDeTeste;
import br.com.alelo.consumer.consumerpat.fabricas.NumerosDeCartaoDeTeste;
import br.com.alelo.consumer.consumerpat.medicao.CartaoDeDisputa;
import br.com.alelo.consumer.consumerpat.medicao.ClienteHttp;
import br.com.alelo.consumer.consumerpat.medicao.OperacoesDoAlvo;
import br.com.alelo.consumer.consumerpat.medicao.OperacoesDoNovo;
import br.com.alelo.consumer.consumerpat.medicao.RespostaHttp;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Faz as tentativas da bateria contra o sistema novo, pelas rotas {@code /api/v1/*}.
 */
public final class SondaDoNovo implements SondaDeSeguranca {

    private static final String CONSUMIDORES = "/api/v1/consumidores";

    private static final String CARTAO_INEXISTENTE = "9999999999999999";

    private final ClienteHttp http;

    private final OperacoesDoNovo operacoes;

    /**
     * Cria a sonda sobre o sistema novo.
     *
     * @param http o cliente do endereço do sistema novo
     */
    public SondaDoNovo(ClienteHttp http) {
        this.http = http;
        this.operacoes = new OperacoesDoNovo(http);
    }

    private static String codificar(String texto) {
        return URLEncoder.encode(texto, StandardCharsets.UTF_8);
    }

    @Override
    public String nome() {
        return "novo";
    }

    @Override
    public OperacoesDoAlvo operacoes() {
        return operacoes;
    }

    @Override
    public RespostaHttp atualizarMandandoSaldo(CartaoDeDisputa cartao, String saldo)
            throws Exception {
        return http.requisitar("PUT", cartao.referencia(),
                CorposDaApi.atualizacaoComSaldo(DocumentosDeTeste.proximo(), saldo),
                Map.of("Content-Type", "application/json"));
    }

    @Override
    public RespostaHttp comprarPorGet(CartaoDeDisputa cartao, String valor) throws Exception {
        return http.get("/api/v1/compras?tipoEstabelecimento=2&nomeEstabelecimento=E"
                + "&numeroCartao=" + cartao.numero() + "&descricaoProduto=P&valor=" + valor);
    }

    @Override
    public RespostaHttp cadastrarComNome(String nome) throws Exception {
        return http.post(CONSUMIDORES,
                CorposDaApi.cadastroComNome(nome, DocumentosDeTeste.proximo(), TipoCartao.FARMACIA,
                        NumerosDeCartaoDeTeste.proximo()));
    }

    @Override
    public RespostaHttp cadastrarComCampoDesconhecido() throws Exception {
        var corpo = CorposDaApi.cadastro(DocumentosDeTeste.proximo(), TipoCartao.FARMACIA,
                NumerosDeCartaoDeTeste.proximo());
        return http.post(CONSUMIDORES, corpo.replaceFirst("^\\{", "{\"hacker\":\"1\","));
    }

    @Override
    public RespostaHttp cadastrarComJsonMalformado() throws Exception {
        return http.post(CONSUMIDORES, "{\"dados\":");
    }

    @Override
    public RespostaHttp comprarComNumeroDeCartao(String numero) throws Exception {
        return http.post("/api/v1/compras",
                CorposDaApi.compra(TipoCartao.FARMACIA, numero, "1.00"));
    }

    @Override
    public RespostaHttp listar(String ordenacao, String direcao) throws Exception {
        return http.get(CONSUMIDORES + "?ordenacao=" + codificar(ordenacao) + "&direcao="
                + codificar(direcao));
    }

    @Override
    public RespostaHttp creditarCartaoInexistente() throws Exception {
        return http.post("/api/v1/creditos",
                CorposDaApi.credito(CARTAO_INEXISTENTE, TipoCartao.FARMACIA, "1.00"));
    }

    @Override
    public ExposicaoDeDados exporDadosDeConsumidor() throws Exception {
        var documento = DocumentosDeTeste.proximo();
        var cartao = NumerosDeCartaoDeTeste.proximo();
        var cadastro = http.post(CONSUMIDORES,
                CorposDaApi.cadastro(documento, TipoCartao.FARMACIA, cartao));
        var leitura = http.get(cadastro.localizacao().orElseThrow());
        return new ExposicaoDeDados(cadastro.texto() + leitura.texto(), documento, cartao);
    }

    @Override
    public RespostaHttp requisicaoValida() throws Exception {
        return http.get("/api/v1/saude");
    }

    @Override
    public RespostaHttp trace() throws Exception {
        return http.requisitar("TRACE", CONSUMIDORES, "", Map.of());
    }

    @Override
    public RespostaHttp preVooDeOrigemEstranha() throws Exception {
        return http.requisitar("OPTIONS", CONSUMIDORES, "", Map.of("Origin",
                "http://origem-estranha.exemplo", "Access-Control-Request-Method", "POST"));
    }

    @Override
    public RespostaHttp consoleDoH2() throws Exception {
        return http.get("/h2-console");
    }

    @Override
    public RespostaHttp swagger() throws Exception {
        return http.get("/swagger-ui/index.html");
    }

    @Override
    public RespostaHttp especificacao() throws Exception {
        return http.get("/v3/api-docs");
    }
}
