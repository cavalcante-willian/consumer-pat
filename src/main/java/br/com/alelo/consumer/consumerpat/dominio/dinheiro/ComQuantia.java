package br.com.alelo.consumer.consumerpat.dominio.dinheiro;

import java.math.BigDecimal;

/**
 * Papel de quem tem uma quantia de dinheiro.
 *
 * <p>
 * É a operação-base de que as outras são escritas: somar, subtrair, multiplicar e comparar só
 * precisam ler o valor, e por isso cada uma vive em seu arquivo, sem depender de como o
 * {@link Dinheiro} guarda a quantia.
 */
public interface ComQuantia {

    /**
     * A quantia, já na escala de {@value Dinheiro#ESCALA} casas.
     *
     * @return o valor monetário
     */
    BigDecimal valor();
}
