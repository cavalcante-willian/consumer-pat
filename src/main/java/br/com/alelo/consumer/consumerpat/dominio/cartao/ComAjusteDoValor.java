package br.com.alelo.consumer.consumerpat.dominio.cartao;

import java.math.BigDecimal;

/**
 * Papel de um tipo de cartão que altera o valor da compra por uma fração.
 *
 * <p>
 * É a operação-base de que o cálculo do valor cobrado é escrito: desconto e acréscimo são o mesmo
 * conceito com sinal contrário, e o ajuste é guardado como fração, nunca como porcentagem.
 */
public interface ComAjusteDoValor {

    /**
     * O ajuste do valor da compra, em fração e com sinal: -0,10 é um desconto de 10%, 0,00 é nenhum
     * ajuste e 0,35 é um acréscimo de 35%.
     *
     * @return a fração que se soma a 1 para obter o fator do valor cobrado
     */
    BigDecimal ajusteDoValor();
}
