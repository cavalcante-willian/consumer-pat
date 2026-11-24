/**
 * Erros de regra de negócio: a entrada é válida, e uma regra a recusa diante do estado.
 *
 * <p>
 * É diferente de um erro de validação: aqui o dado está bem formado, e o que o impede é uma
 * condição do sistema, como um saldo que não cobre a compra. A resposta é por isso uma recusa do
 * pedido, e não um campo malformado.
 */
package br.com.alelo.consumer.consumerpat.dominio.erros.regradenegocio;
