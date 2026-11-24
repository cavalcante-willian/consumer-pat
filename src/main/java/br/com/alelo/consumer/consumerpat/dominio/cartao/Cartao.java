package br.com.alelo.consumer.consumerpat.dominio.cartao;

import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;

import java.util.Objects;
import java.util.Optional;

/**
 * Um cartão de benefício: o número, o tipo e o saldo.
 *
 * <p>
 * É imutável. Debitar e creditar não alteram o cartão, devolvem outro com o novo saldo, e quem
 * chama decide o que fazer com o estado antigo. O saldo nunca é negativo: um cartão com saldo
 * negativo não existe, e tentar criá-lo é erro de quem construiu. As duas operações são papéis em
 * arquivos próprios, {@link Debitavel} e {@link Creditavel}, escritos sobre {@link ComSaldo}.
 *
 * <p>
 * O {@code toString()} herda a máscara do {@link NumeroDeCartao}, de modo que imprimir um cartão
 * não revela o número.
 *
 * @param numero o número do cartão, na forma canônica
 * @param tipo   o tipo, que define a regra de cobrança
 * @param saldo  o saldo disponível, nunca negativo
 */
public record Cartao(NumeroDeCartao numero, TipoCartao tipo, Dinheiro saldo)
        implements Debitavel, Creditavel {

    /**
     * Cria o cartão.
     *
     * @param numero o número do cartão
     * @param tipo   o tipo do cartão
     * @param saldo  o saldo inicial, zero ou mais
     * @throws NullPointerException     se o número ou o tipo é nulo
     * @throws IllegalArgumentException se o saldo é nulo ou negativo
     */
    public Cartao {
        Objects.requireNonNull(numero, "O cartão precisa de um número");
        Objects.requireNonNull(tipo, "O cartão precisa de um tipo");
        saldo = Optional.ofNullable(saldo)
                .filter(disponivel -> disponivel.compareTo(Dinheiro.ZERO) >= 0)
                .orElseThrow(() -> new IllegalArgumentException(
                        "O saldo do cartão não pode ser nulo nem negativo"));
    }

    @Override
    public Cartao comSaldo(Dinheiro novoSaldo) {
        return new Cartao(numero, tipo, novoSaldo);
    }
}
