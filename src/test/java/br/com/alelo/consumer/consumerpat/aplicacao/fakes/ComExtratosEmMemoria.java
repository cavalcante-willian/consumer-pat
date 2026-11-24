package br.com.alelo.consumer.consumerpat.aplicacao.fakes;

import br.com.alelo.consumer.consumerpat.dominio.extrato.Extrato;

import java.util.List;

/**
 * Papel de um fake do extrato que tem uma memória: é a operação-base de que as outras são escritas.
 */
public interface ComExtratosEmMemoria {

    /**
     * Os extratos registrados, em ordem. É a lista viva, e não uma cópia.
     *
     * @return a lista de extratos
     */
    List<Extrato> extratosEmMemoria();
}
