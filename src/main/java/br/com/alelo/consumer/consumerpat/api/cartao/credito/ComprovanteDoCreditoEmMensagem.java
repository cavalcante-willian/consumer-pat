package br.com.alelo.consumer.consumerpat.api.cartao.credito;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoComprovanteCredito;

/**
 * Converte o comprovante interno do crédito na mensagem de resposta.
 *
 * <p>
 * É a única peça que sabe como o comprovante vira o que o cliente lê: o número já sai mascarado,
 * porque o tipo o mascara ao virar texto, e o dinheiro sai com a escala toda.
 */
public final class ComprovanteDoCreditoEmMensagem {

    private ComprovanteDoCreditoEmMensagem() {
    }

    /**
     * Converte o comprovante.
     *
     * @param comprovante o comprovante do caso de uso
     * @return a mensagem de resposta
     */
    public static MensagemComprovanteCredito montar(DtoComprovanteCredito comprovante) {
        return new MensagemComprovanteCredito(comprovante.numeroCartao().toString(),
                comprovante.tipo().name(), comprovante.valorCreditado().valor().toPlainString(),
                comprovante.saldoAtual().valor().toPlainString());
    }
}
