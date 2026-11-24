package br.com.alelo.consumer.consumerpat.api.cartao.credito;

/**
 * A resposta de um crédito efetivado.
 *
 * <p>
 * O número do cartão sai mascarado, e o dinheiro sai como texto com a escala toda.
 *
 * @param numeroCartao   o número do cartão creditado, só com os quatro últimos dígitos visíveis
 * @param tipo           o tipo do cartão creditado
 * @param valorCreditado o valor que foi somado ao saldo
 * @param saldoAtual     o saldo do cartão depois do crédito
 */
public record MensagemComprovanteCredito(String numeroCartao, String tipo, String valorCreditado,
        String saldoAtual) {
}
