package br.com.alelo.consumer.consumerpat.aplicacao.fakes;

/**
 * Papel de um fake do cartão que tem uma memória: é a operação-base de que as outras são escritas.
 */
public interface ComCartoesEmMemoria {

    /**
     * A memória do fake.
     *
     * @return o estado compartilhado pelas operações
     */
    CartoesEmMemoria memoria();
}
