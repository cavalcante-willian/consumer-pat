package br.com.alelo.consumer.consumerpat.aplicacao.dtos;

import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;

import java.time.Instant;

/**
 * O comprovante de uma compra efetivada.
 *
 * <p>
 * Traz os dois valores, o informado e o cobrado, para que quem comprou veja o ajuste do tipo, e o
 * saldo que sobrou. É o mesmo conteúdo do extrato gravado, mais o saldo. O número é um
 * {@link NumeroDeCartao}, cujo texto é mascarado, de modo que o comprovante não revela o cartão.
 *
 * @param numeroCartao   o cartão debitado
 * @param tipo           o tipo do cartão debitado
 * @param valorInformado o valor da compra, antes do ajuste do tipo
 * @param valorCobrado   o valor debitado, já com o ajuste do tipo
 * @param saldoRestante  o saldo do cartão depois do débito
 * @param instante       o instante da compra
 */
public record DtoComprovanteCompra(NumeroDeCartao numeroCartao, TipoCartao tipo,
        Dinheiro valorInformado, Dinheiro valorCobrado, Dinheiro saldoRestante, Instant instante) {
}
