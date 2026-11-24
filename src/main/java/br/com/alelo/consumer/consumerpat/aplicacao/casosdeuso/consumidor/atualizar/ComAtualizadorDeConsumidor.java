package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.atualizar;

import br.com.alelo.consumer.consumerpat.dominio.portas.AtualizadorDeConsumidor;

/**
 * Operação-base do papel de atualizar o cadastro: expõe quem grava os dados novos.
 */
public interface ComAtualizadorDeConsumidor {

    /**
     * Quem grava os dados do consumidor.
     *
     * @return a porta de atualização
     */
    AtualizadorDeConsumidor atualizador();
}
