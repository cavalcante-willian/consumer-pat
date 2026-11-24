package br.com.alelo.consumer.consumerpat.aplicacao.dtos;

import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;

/**
 * O pedido de um crédito, já com os tipos do domínio.
 *
 * <p>
 * O cartão é identificado pelo número e pelo tipo. Informar o tipo evita adivinhá-lo pelo número,
 * que era o que o legado fazia, procurando o número nas três colunas em sequência. Como na compra,
 * a borda converte o que recebeu de fora antes de montar o pedido.
 *
 * @param numeroCartao o cartão a creditar
 * @param tipo         o tipo do cartão a creditar
 * @param valor        o valor a creditar
 */
public record DtoCredito(NumeroDeCartao numeroCartao, TipoCartao tipo, Dinheiro valor) {
}
