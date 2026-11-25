package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.compra;

/**
 * Operação-base do papel de registrar uma compra: expõe quem grava o que a compra deixa.
 */
public interface ComGravadorDaCompra {

    /**
     * Quem grava o cartão debitado e o extrato.
     *
     * @return o gravador da compra
     */
    GravadorDaCompra gravador();
}
