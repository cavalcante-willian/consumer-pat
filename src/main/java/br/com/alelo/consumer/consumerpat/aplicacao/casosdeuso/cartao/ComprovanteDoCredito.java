package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoComprovanteCredito;
import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoCredito;
import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;

/**
 * Monta o comprovante de um crédito efetivado.
 *
 * <p>
 * É a única peça que sabe como o resultado do crédito vira resposta: o que foi pedido, e o saldo
 * que o cartão tem agora.
 */
public final class ComprovanteDoCredito {

    private ComprovanteDoCredito() {
    }

    /**
     * Monta o comprovante.
     *
     * @param credito   o pedido de crédito
     * @param creditado o cartão depois do crédito, de onde vem o saldo atual
     * @return o comprovante
     */
    public static DtoComprovanteCredito montar(DtoCredito credito, Cartao creditado) {
        return new DtoComprovanteCredito(credito.numeroCartao(), credito.tipo(), credito.valor(),
                creditado.saldo());
    }
}
