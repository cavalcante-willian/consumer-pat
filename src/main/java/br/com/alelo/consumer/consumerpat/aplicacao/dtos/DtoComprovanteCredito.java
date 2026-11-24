package br.com.alelo.consumer.consumerpat.aplicacao.dtos;

import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;

/**
 * O comprovante de um crédito efetivado.
 *
 * @param numeroCartao   o cartão creditado; o texto é mascarado
 * @param tipo           o tipo do cartão creditado
 * @param valorCreditado o valor que foi somado ao saldo
 * @param saldoAtual     o saldo do cartão depois do crédito
 */
public record DtoComprovanteCredito(NumeroDeCartao numeroCartao, TipoCartao tipo,
        Dinheiro valorCreditado, Dinheiro saldoAtual) {
}
