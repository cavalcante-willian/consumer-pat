package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.portas.BuscadorDeConsumidor;

/**
 * Operação-base dos papéis que precisam ler um consumidor: expõe quem o busca por id.
 */
public interface ComBuscadorDeConsumidor {

    /**
     * Quem busca o consumidor por id.
     *
     * @return a porta de busca
     */
    BuscadorDeConsumidor buscador();
}
