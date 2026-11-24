package br.com.alelo.consumer.consumerpat.fabricas;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;

/**
 * Papel de um modelo de cartão de teste: tem os dados de que as operações da fábrica são escritas.
 *
 * <p>
 * O número e o saldo são texto, como o teste os escreve, e só viram {@code NumeroDeCartao} e
 * {@code Dinheiro} quando o cartão é construído, de modo que um valor inválido falha ali.
 */
public interface ComDadosDeCartao {

    /**
     * O tipo do cartão.
     *
     * @return o tipo
     */
    TipoCartao tipo();

    /**
     * O número do cartão, só dígitos.
     *
     * @return o número
     */
    String numero();

    /**
     * O saldo do cartão, como decimal com ponto.
     *
     * @return o saldo
     */
    String saldo();
}
