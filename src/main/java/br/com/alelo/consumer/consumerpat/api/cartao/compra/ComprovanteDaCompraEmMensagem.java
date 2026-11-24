package br.com.alelo.consumer.consumerpat.api.cartao.compra;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoComprovanteCompra;

/**
 * Converte o comprovante interno da compra na mensagem de resposta.
 *
 * <p>
 * É a única peça que sabe como o comprovante vira o que o cliente lê: o número já sai mascarado,
 * porque o tipo o mascara ao virar texto, e o dinheiro sai com a escala toda.
 */
public final class ComprovanteDaCompraEmMensagem {

    private ComprovanteDaCompraEmMensagem() {
    }

    /**
     * Converte o comprovante.
     *
     * @param comprovante o comprovante do caso de uso
     * @return a mensagem de resposta
     */
    public static MensagemComprovanteCompra montar(DtoComprovanteCompra comprovante) {
        return new MensagemComprovanteCompra(comprovante.numeroCartao().toString(),
                comprovante.tipo().name(), comprovante.valorInformado().valor().toPlainString(),
                comprovante.valorCobrado().valor().toPlainString(),
                comprovante.saldoRestante().valor().toPlainString(), comprovante.instante());
    }
}
