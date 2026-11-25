/**
 * Casos de uso do cartão, um subpacote por operação: {@code compra}, que registra uma compra, e
 * {@code credito}, que credita um saldo.
 *
 * <p>
 * Em cada um a operação mora num papel de método {@code default}, com o {@code @Transactional}, e a
 * classe do caso só guarda as dependências. A raiz guarda o que os dois compartilham: a
 * operação-base que expõe quem busca o cartão para escrever.
 */
package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao;
