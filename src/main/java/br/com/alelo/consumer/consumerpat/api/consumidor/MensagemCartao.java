package br.com.alelo.consumer.consumerpat.api.consumidor;

/**
 * Um cartão do consumidor, como a resposta o mostra.
 *
 * <p>
 * O número vem mascarado, e o saldo como texto com a escala toda, como nos comprovantes: dinheiro
 * nunca sai como número de JSON, que perderia precisão.
 *
 * @param numero o número mascarado
 * @param tipo   o tipo, pelo nome
 * @param saldo  o saldo, em texto, sem notação científica
 */
public record MensagemCartao(String numero, String tipo, String saldo) {
}
