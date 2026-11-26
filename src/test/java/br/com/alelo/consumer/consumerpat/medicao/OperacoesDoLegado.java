package br.com.alelo.consumer.consumerpat.medicao;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import com.jayway.jsonpath.JsonPath;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fala com o original, o legado, pelas rotas {@code /consumer/*}.
 *
 * <p>
 * Não depende de nenhuma classe do legado: conversa por HTTP com o sistema que estiver no endereço,
 * que é o original extraído do histórico. Por isso continua servindo quando o legado sai desta
 * árvore.
 *
 * <p>
 * O que o legado faz de diferente aparece aqui, e não nos cenários: o cadastro e o crédito gravam
 * mas respondem {@code 404}, porque o método é {@code void} e sem corpo de resposta, e o Spring
 * tenta resolver uma visão; a listagem devolve a tabela inteira sem paginação; não há consulta por
 * id; e crédito e compra são {@code GET}. O saldo só se lê pela listagem, e por isso a disputa roda
 * com a base ainda quase vazia.
 */
public final class OperacoesDoLegado implements OperacoesDoAlvo {

    private static final int BASE_DA_DISPUTA = 900_000_000;

    private final ClienteHttp http;

    private final AtomicInteger sequenciaDaDisputa = new AtomicInteger();

    /**
     * Cria as operações sobre o legado.
     *
     * @param http o cliente do endereço do legado
     */
    public OperacoesDoLegado(ClienteHttp http) {
        this.http = http;
    }

    @Override
    public String nome() {
        return "legado";
    }

    @Override
    public RespostaHttp cadastrar(int indice) throws Exception {
        return http.post("/consumer/createConsumer", CorposDoLegado.cadastro(indice));
    }

    @Override
    public List<Pedido> listagens(int total) {
        return List.of(new Pedido("listagem-primeira-pagina",
                "GET /consumer/consumerList: a tabela inteira, sem paginação",
                () -> http.get("/consumer/consumerList")));
    }

    @Override
    public List<Pedido> consultas(int total) {
        return List.of();
    }

    @Override
    public RespostaHttp creditar(TipoCartao tipo, int indice, String valor) throws Exception {
        return http.get("/consumer/setcardbalance?cardNumber="
                + CorposDoLegado.numeroDoCartao(indice, tipo) + "&value=" + valor);
    }

    @Override
    public RespostaHttp comprar(TipoCartao tipo, int indice, String valor) throws Exception {
        return comprarPeloNumero(tipo, CorposDoLegado.numeroDoCartao(indice, tipo), valor);
    }

    @Override
    public CartaoDeDisputa prepararFarmaciaComSaldo(String saldo) throws Exception {
        var sequencia = sequenciaDaDisputa.incrementAndGet();
        var numero = BASE_DA_DISPUTA + sequencia;
        http.post("/consumer/createConsumer",
                CorposDoLegado.cadastroDeFarmacia(BASE_DA_DISPUTA + sequencia, numero, saldo));
        return new CartaoDeDisputa(String.valueOf(numero), String.valueOf(numero));
    }

    @Override
    public int comprarNaFarmacia(CartaoDeDisputa cartao, String valor) throws Exception {
        return comprarPeloNumero(TipoCartao.FARMACIA, Integer.parseInt(cartao.numero()), valor)
                .status();
    }

    @Override
    public BigDecimal saldoDe(CartaoDeDisputa cartao) throws Exception {
        var corpo = http.get("/consumer/consumerList").texto();
        List<Double> saldos = JsonPath.read(corpo,
                "$[?(@.drugstoreNumber==" + cartao.referencia() + ")].drugstoreCardBalance");
        return BigDecimal.valueOf(saldos.get(0));
    }

    private RespostaHttp comprarPeloNumero(TipoCartao tipo, int numeroDoCartao, String valor)
            throws Exception {
        return http.get("/consumer/buy?establishmentType=" + tipo.codigoDeEstabelecimento()
                + "&establishmentName=Estabelecimento&cardNumber=" + numeroDoCartao
                + "&productDescription=Produto&value=" + valor);
    }
}
