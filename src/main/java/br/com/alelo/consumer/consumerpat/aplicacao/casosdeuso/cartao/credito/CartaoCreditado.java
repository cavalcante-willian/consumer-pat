package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.credito;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoComprovanteCredito;
import br.com.alelo.consumer.consumerpat.dominio.log.SaidaDeLogPorta;

import static br.com.alelo.consumer.consumerpat.dominio.log.CampoDeLog.campo;

/**
 * Registra que um cartão foi creditado: o evento {@code cartao.creditado}.
 *
 * <p>
 * Leva o cartão mascarado, o tipo e o valor creditado, e não o saldo que ficou: o saldo é dado do
 * cliente, e o valor creditado basta para auditar o fato. O log não é a trilha financeira; ela é o
 * extrato.
 */
public final class CartaoCreditado {

    private CartaoCreditado() {
    }

    /**
     * Registra o crédito.
     *
     * @param saida       a porta de log
     * @param comprovante o comprovante do crédito efetivado
     */
    public static void registrar(SaidaDeLogPorta saida, DtoComprovanteCredito comprovante) {
        saida.informacao("cartao.creditado", campo("cartao", comprovante.numeroCartao()),
                campo("tipoCartao", comprovante.tipo()),
                campo("valor", comprovante.valorCreditado().valor().toPlainString()));
    }
}
