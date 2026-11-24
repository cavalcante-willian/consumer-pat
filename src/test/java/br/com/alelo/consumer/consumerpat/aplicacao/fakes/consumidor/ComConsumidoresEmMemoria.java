package br.com.alelo.consumer.consumerpat.aplicacao.fakes.consumidor;

/**
 * Operação-base dos papéis do fake do consumidor: o estado em memória.
 */
public interface ComConsumidoresEmMemoria {

    /**
     * O estado compartilhado.
     *
     * @return os consumidores em memória
     */
    ConsumidoresEmMemoria memoria();
}
