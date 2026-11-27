package br.com.alelo.consumer.consumerpat.medicao;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.fabricas.CorposDaApi;
import br.com.alelo.consumer.consumerpat.fabricas.DocumentosDeTeste;
import br.com.alelo.consumer.consumerpat.fabricas.NumerosDeCartaoDeTeste;
import com.jayway.jsonpath.JsonPath;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fala com o sistema novo, pelas rotas {@code /api/v1/*}.
 *
 * <p>
 * O saldo de um cartão novo começa em zero, e só o crédito o move, de modo que quem mede uma compra
 * antes precisa ter creditado. A listagem é paginada, e a consulta por id parte de um id lido da
 * própria listagem, para não supor a numeração do banco.
 */
public final class OperacoesDoNovo implements OperacoesDoAlvo {

    private static final String CONSUMIDORES = "/api/v1/consumidores";

    private static final long BASE_DOS_DOCUMENTOS = 80_000_000_000L;

    private static final long BASE_DOS_CARTOES = 8_000_000_000_000_000L;

    private static final int TIPOS = TipoCartao.values().length;

    private static final int ITENS_DA_PAGINA = 20;

    private static final int ITENS_DA_PAGINA_GRANDE = 100;

    private final ClienteHttp http;

    /**
     * Cria as operações sobre o sistema novo.
     *
     * @param http o cliente do endereço do sistema novo
     */
    public OperacoesDoNovo(ClienteHttp http) {
        this.http = http;
    }

    @Override
    public String nome() {
        return "novo";
    }

    /**
     * O documento do consumidor de um índice da base.
     *
     * @param indice o índice
     * @return onze dígitos
     */
    static String documento(int indice) {
        return Long.toString(BASE_DOS_DOCUMENTOS + indice);
    }

    /**
     * O número do cartão de um tipo, do consumidor de um índice da base.
     *
     * @param indice o índice do consumidor
     * @param tipo   o tipo do cartão
     * @return dezesseis dígitos, único na base
     */
    static String numeroDoCartao(int indice, TipoCartao tipo) {
        return Long.toString(BASE_DOS_CARTOES + (long) indice * TIPOS + tipo.ordinal());
    }

    @Override
    public RespostaHttp cadastrar(int indice) throws Exception {
        return http.post(CONSUMIDORES,
                CorposDaApi.cadastroComTresCartoes(documento(indice),
                        numeroDoCartao(indice, TipoCartao.ALIMENTACAO),
                        numeroDoCartao(indice, TipoCartao.COMBUSTIVEL),
                        numeroDoCartao(indice, TipoCartao.FARMACIA)));
    }

    @Override
    public List<Pedido> listagens(int total) {
        var ultima = (total + ITENS_DA_PAGINA - 1) / ITENS_DA_PAGINA;
        return List.of(
                pagina("listagem-primeira-pagina", "a primeira página, de 20", 1, ITENS_DA_PAGINA),
                pagina("listagem-pagina-do-meio", "a página do meio, de 20",
                        Math.max(1, ultima / 2), ITENS_DA_PAGINA),
                pagina("listagem-ultima-pagina", "a última página, de 20", ultima, ITENS_DA_PAGINA),
                pagina("listagem-primeira-pagina-de-100", "a primeira página, de 100", 1,
                        ITENS_DA_PAGINA_GRANDE));
    }

    @Override
    public List<Pedido> consultas(int total) throws Exception {
        var meio = Math.max(1, (total + ITENS_DA_PAGINA - 1) / ITENS_DA_PAGINA / 2);
        var corpo = http.get(CONSUMIDORES + "?pagina=" + meio + "&tamanhoPagina=1").texto();
        var id = String.valueOf(JsonPath.<Object>read(corpo, "$.itens[0].id"));
        return List.of(new Pedido("consulta-por-id",
                "GET " + CONSUMIDORES + "/{id}: um consumidor do meio da base, com os cartões",
                () -> http.get(CONSUMIDORES + "/" + id)));
    }

    @Override
    public RespostaHttp creditar(TipoCartao tipo, int indice, String valor) throws Exception {
        return http.post("/api/v1/creditos",
                CorposDaApi.credito(numeroDoCartao(indice, tipo), tipo, valor));
    }

    @Override
    public RespostaHttp comprar(TipoCartao tipo, int indice, String valor) throws Exception {
        return http.post("/api/v1/compras",
                CorposDaApi.compra(tipo, numeroDoCartao(indice, tipo), valor));
    }

    @Override
    public CartaoDeDisputa prepararFarmaciaComSaldo(String saldo) throws Exception {
        var numero = NumerosDeCartaoDeTeste.proximo();
        var cadastro = http.post(CONSUMIDORES,
                CorposDaApi.cadastro(DocumentosDeTeste.proximo(), TipoCartao.FARMACIA, numero));
        assertThat(cadastro.status()).as("cadastro do consumidor da disputa").isEqualTo(201);
        var credito = http.post("/api/v1/creditos",
                CorposDaApi.credito(numero, TipoCartao.FARMACIA, saldo));
        assertThat(credito.status()).as("crédito inicial da disputa").isEqualTo(201);
        return new CartaoDeDisputa(numero, cadastro.localizacao().orElseThrow());
    }

    @Override
    public int comprarNaFarmacia(CartaoDeDisputa cartao, String valor) throws Exception {
        return http.post("/api/v1/compras",
                CorposDaApi.compra(TipoCartao.FARMACIA, cartao.numero(), valor)).status();
    }

    @Override
    public BigDecimal saldoDe(CartaoDeDisputa cartao) throws Exception {
        var corpo = http.get(cartao.referencia()).texto();
        return new BigDecimal(JsonPath.<String>read(corpo, "$.cartoes[0].saldo"));
    }

    private Pedido pagina(String chave, String descricao, int pagina, int tamanho) {
        return new Pedido(chave,
                "GET " + CONSUMIDORES + "?pagina=" + pagina + "&tamanhoPagina=" + tamanho + ": "
                        + descricao,
                () -> http.get(CONSUMIDORES + "?pagina=" + pagina + "&tamanhoPagina=" + tamanho));
    }
}
