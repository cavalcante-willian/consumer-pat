package br.com.alelo.consumer.consumerpat.dominio.extrato;

import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;

import java.time.Instant;
import java.util.Objects;

/**
 * O registro de uma compra efetivada.
 *
 * <p>
 * Guarda os dois valores, o informado e o cobrado, porque a diferença entre eles é o ajuste do tipo
 * do cartão: com só o cobrado, um desconto de 10% sobre 33,33 seria indistinguível de uma compra de
 * 29,997. O instante vem de quem registra, e não é lido do relógio aqui, para que o extrato seja um
 * dado e o teste o possa fixar.
 *
 * <p>
 * É imutável e não tem operação: nenhuma interface oferece alterá-lo, e o que ninguém oferece
 * ninguém chama por engano. O {@code toString()} herda a máscara do {@link NumeroDeCartao}.
 *
 * @param nomeDoEstabelecimento o estabelecimento em que a compra foi feita
 * @param descricaoDoProduto    o que foi comprado
 * @param dataDaCompra          o instante da compra, em UTC
 * @param numeroDoCartao        o cartão debitado
 * @param tipo                  o tipo do cartão debitado
 * @param valorInformado        o valor da compra, antes do ajuste do tipo
 * @param valorCobrado          o valor debitado, já com o ajuste do tipo
 */
public record Extrato(String nomeDoEstabelecimento, String descricaoDoProduto, Instant dataDaCompra,
        NumeroDeCartao numeroDoCartao, TipoCartao tipo, Dinheiro valorInformado,
        Dinheiro valorCobrado) {

    /**
     * Cria o extrato.
     *
     * @param nomeDoEstabelecimento o estabelecimento
     * @param descricaoDoProduto    o produto
     * @param dataDaCompra          o instante da compra
     * @param numeroDoCartao        o cartão debitado
     * @param tipo                  o tipo do cartão
     * @param valorInformado        o valor antes do ajuste
     * @param valorCobrado          o valor depois do ajuste
     * @throws NullPointerException se qualquer componente é nulo
     */
    public Extrato {
        Objects.requireNonNull(nomeDoEstabelecimento, "O extrato precisa do estabelecimento");
        Objects.requireNonNull(descricaoDoProduto, "O extrato precisa da descrição do produto");
        Objects.requireNonNull(dataDaCompra, "O extrato precisa da data da compra");
        Objects.requireNonNull(numeroDoCartao, "O extrato precisa do número do cartão");
        Objects.requireNonNull(tipo, "O extrato precisa do tipo do cartão");
        Objects.requireNonNull(valorInformado, "O extrato precisa do valor informado");
        Objects.requireNonNull(valorCobrado, "O extrato precisa do valor cobrado");
    }
}
