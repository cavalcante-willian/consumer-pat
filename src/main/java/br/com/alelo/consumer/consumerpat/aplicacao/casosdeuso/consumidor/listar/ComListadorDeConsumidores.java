package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.listar;

import br.com.alelo.consumer.consumerpat.dominio.portas.ListadorDeConsumidores;

/**
 * Operação-base do papel de listar: expõe quem lista os consumidores.
 */
public interface ComListadorDeConsumidores {

    /**
     * Quem lista os consumidores, uma página por vez.
     *
     * @return a porta de listagem
     */
    ListadorDeConsumidores listador();
}
