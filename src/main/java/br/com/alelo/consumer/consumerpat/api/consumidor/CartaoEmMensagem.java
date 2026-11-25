package br.com.alelo.consumer.consumerpat.api.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;

/**
 * Converte um cartão do domínio na mensagem de saída.
 */
public final class CartaoEmMensagem {

    private CartaoEmMensagem() {
    }

    /**
     * Monta a mensagem.
     *
     * @param cartao o cartão do domínio
     * @return o número mascarado, o tipo pelo nome e o saldo como texto
     */
    public static MensagemCartao montar(Cartao cartao) {
        return new MensagemCartao(cartao.numero().toString(), cartao.tipo().name(),
                cartao.saldo().valor().toPlainString());
    }
}
