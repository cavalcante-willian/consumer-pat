package br.com.alelo.consumer.consumerpat.medicao;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * O que o driver sabe pedir a um sistema, sem saber como ele o chama.
 *
 * <p>
 * É o que permite medir o legado e o novo com o mesmo código: cada sistema traduz as operações para
 * as suas rotas e os seus corpos, e os cenários só pedem. O crédito e a compra, que são as mesmas
 * nos dois, são montados aqui a partir das duas operações de uma chamada só.
 */
public interface OperacoesDoAlvo {

    /** Como se lê o nome de cada tipo de cartão, com acento. */
    Map<TipoCartao, String> NOMES_DOS_TIPOS = Map.of(TipoCartao.ALIMENTACAO, "alimentação",
            TipoCartao.FARMACIA, "farmácia", TipoCartao.COMBUSTIVEL, "combustível");

    /** A medida de um crédito. */
    String VALOR_DO_CREDITO = "10.00";

    /** A medida de uma compra. */
    String VALOR_DA_COMPRA = "1.00";

    /**
     * O nome do sistema, para o relatório.
     *
     * @return {@code legado} ou {@code novo}
     */
    String nome();

    /**
     * Cadastra o consumidor de um índice, com um cartão de cada tipo.
     *
     * @param indice o índice do consumidor, que define o documento e os números dos cartões
     * @return a resposta, qualquer que seja o status: o legado grava e responde {@code 404}
     * @throws Exception se a chamada falha
     */
    RespostaHttp cadastrar(int indice) throws Exception;

    /**
     * As listagens que o sistema oferece, com o rótulo comum a ambos.
     *
     * @param total quantos consumidores a base tem
     * @return os pedidos de listagem
     * @throws Exception se preparar o pedido falha
     */
    List<Pedido> listagens(int total) throws Exception;

    /**
     * As consultas de um consumidor pelo id, se o sistema as tem.
     *
     * @param total quantos consumidores a base tem
     * @return os pedidos, ou nenhum se o sistema não tem a operação
     * @throws Exception se preparar o pedido falha
     */
    List<Pedido> consultas(int total) throws Exception;

    /**
     * Credita um cartão.
     *
     * @param tipo   o tipo do cartão
     * @param indice o índice do consumidor dono
     * @param valor  o valor, como decimal com ponto
     * @return a resposta
     * @throws Exception se a chamada falha
     */
    RespostaHttp creditar(TipoCartao tipo, int indice, String valor) throws Exception;

    /**
     * Compra num cartão.
     *
     * @param tipo   o tipo do cartão, que é o do estabelecimento
     * @param indice o índice do consumidor dono
     * @param valor  o valor, como decimal com ponto
     * @return a resposta
     * @throws Exception se a chamada falha
     */
    RespostaHttp comprar(TipoCartao tipo, int indice, String valor) throws Exception;

    /**
     * Cria um consumidor novo só com um cartão de farmácia e o saldo informado.
     *
     * @param saldo o saldo inicial, como decimal com ponto
     * @return o cartão preparado
     * @throws Exception se a preparação falha
     */
    CartaoDeDisputa prepararFarmaciaComSaldo(String saldo) throws Exception;

    /**
     * Compra num cartão de farmácia preparado.
     *
     * @param cartao o cartão
     * @param valor  o valor, como decimal com ponto
     * @return o status da resposta
     * @throws Exception se a chamada falha
     */
    int comprarNaFarmacia(CartaoDeDisputa cartao, String valor) throws Exception;

    /**
     * Lê o saldo de um cartão preparado.
     *
     * @param cartao o cartão
     * @return o saldo
     * @throws Exception se a leitura falha
     */
    BigDecimal saldoDe(CartaoDeDisputa cartao) throws Exception;

    /**
     * Os créditos medidos: cada tipo de cartão, no início, no meio e no fim da base.
     *
     * @param total quantos consumidores a base tem
     * @return nove pedidos
     */
    default List<Pedido> creditos(int total) {
        return PosicaoNaBase.de(total).stream()
                .flatMap(posicao -> Arrays.stream(TipoCartao.values())
                        .map(tipo -> new Pedido(chave("credito", tipo, posicao),
                                descricao("crédito", tipo, posicao),
                                () -> creditar(tipo, posicao.indice(), VALOR_DO_CREDITO))))
                .toList();
    }

    /**
     * As compras medidas: cada tipo de cartão, no início, no meio e no fim da base.
     *
     * @param total quantos consumidores a base tem
     * @return nove pedidos
     */
    default List<Pedido> compras(int total) {
        return PosicaoNaBase.de(total).stream()
                .flatMap(posicao -> Arrays.stream(TipoCartao.values())
                        .map(tipo -> new Pedido(chave("compra", tipo, posicao),
                                descricao("compra", tipo, posicao),
                                () -> comprar(tipo, posicao.indice(), VALOR_DA_COMPRA))))
                .toList();
    }

    private static String chave(String operacao, TipoCartao tipo, PosicaoNaBase posicao) {
        return operacao + "-" + tipo.name().toLowerCase(Locale.ROOT) + "-" + posicao.nome();
    }

    private static String descricao(String operacao, TipoCartao tipo, PosicaoNaBase posicao) {
        return operacao + " no cartão de " + NOMES_DOS_TIPOS.get(tipo) + " do consumidor do "
                + posicao.rotulo() + " da base";
    }
}
