package br.com.alelo.consumer.consumerpat.aplicacao.fakes;

import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;

/**
 * O que identifica um cartão na memória do fake: o número e o tipo, como na porta real.
 *
 * <p>
 * É um tipo, e não um texto montado por concatenação, para que dois cartões com o mesmo número e
 * tipos diferentes sejam chaves diferentes por construção, e não por um separador escolhido bem.
 *
 * @param numero o número do cartão
 * @param tipo   o tipo do cartão
 */
public record ChaveDeCartao(NumeroDeCartao numero, TipoCartao tipo) {
}
