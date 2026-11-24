package br.com.alelo.consumer.consumerpat.api.cartao.compra;

import java.time.Instant;

/**
 * A resposta de uma compra efetivada.
 *
 * <p>
 * O número do cartão sai mascarado, e o dinheiro sai como texto com a escala toda, e nunca como
 * número JSON: um cliente que lê número converte para ponto flutuante e perde a fração de centavo
 * que o sistema guarda. O tipo sai pelo nome, e o instante em UTC.
 *
 * @param numeroCartao   o número do cartão debitado, só com os quatro últimos dígitos visíveis
 * @param tipo           o tipo do cartão debitado
 * @param valorInformado o valor da compra, antes do ajuste do tipo
 * @param valorCobrado   o valor debitado, já com o ajuste do tipo
 * @param saldoRestante  o saldo do cartão depois do débito
 * @param instante       o instante da compra
 */
public record MensagemComprovanteCompra(String numeroCartao, String tipo, String valorInformado,
        String valorCobrado, String saldoRestante, Instant instante) {
}
