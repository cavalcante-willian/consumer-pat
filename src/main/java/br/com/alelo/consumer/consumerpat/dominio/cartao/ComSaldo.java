package br.com.alelo.consumer.consumerpat.dominio.cartao;

import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;

/**
 * Papel de um cartão que tem saldo e sabe se refazer com outro.
 *
 * <p>
 * É a operação-base de que o débito e o crédito são escritos: cada um só calcula o novo saldo e
 * pede ao cartão um igual a ele, mas com esse saldo. O cartão é imutável, então nenhuma operação
 * altera o que recebeu, e quem chama fica com os dois estados.
 */
public interface ComSaldo {

    /**
     * O saldo atual.
     *
     * @return a quantia disponível, nunca negativa
     */
    Dinheiro saldo();

    /**
     * Um cartão igual a este, exceto pelo saldo.
     *
     * @param novoSaldo o saldo do novo cartão
     * @return o novo cartão; este não é alterado
     */
    Cartao comSaldo(Dinheiro novoSaldo);
}
