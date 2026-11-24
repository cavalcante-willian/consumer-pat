/**
 * Os endpoints do cartão, um pacote por operação: {@code compra} e {@code credito}.
 *
 * <p>
 * Cada operação tem a sua mensagem de entrada, a sua de saída e o seu controller. A entrada nunca
 * monta domínio: a mensagem é validada na borda, convertida nos tipos do domínio com as leituras
 * que ele tem, e só então vira pedido do caso de uso. A saída nunca é o objeto interno: é a
 * projeção que o cliente precisa, com o número do cartão mascarado e o dinheiro em texto com a
 * escala toda.
 */
package br.com.alelo.consumer.consumerpat.api.cartao;
