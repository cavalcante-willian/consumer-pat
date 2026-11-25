package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.compra;

import java.time.Clock;

/**
 * Operação-base do papel de registrar uma compra: expõe o relógio de onde vem o instante dela.
 */
public interface ComRelogioDaCompra {

    /**
     * O relógio do sistema, o único da aplicação.
     *
     * @return o relógio
     */
    Clock relogio();
}
