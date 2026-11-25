package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.credito;

import br.com.alelo.consumer.consumerpat.dominio.portas.GravadorDeCartao;

/**
 * Operação-base do papel de creditar um cartão: expõe quem grava o cartão com o novo saldo.
 */
public interface ComGravadorDeCartao {

    /**
     * Quem grava o cartão.
     *
     * @return a porta de gravação
     */
    GravadorDeCartao gravador();
}
