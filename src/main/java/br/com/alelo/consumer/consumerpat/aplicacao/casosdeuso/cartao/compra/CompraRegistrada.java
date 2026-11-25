package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.compra;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoComprovanteCompra;
import br.com.alelo.consumer.consumerpat.dominio.log.SaidaDeLogPorta;

import static br.com.alelo.consumer.consumerpat.dominio.log.CampoDeLog.campo;

/**
 * Registra que uma compra foi efetivada: o evento {@code compra.registrada}.
 *
 * <p>
 * Leva o cartão, que só existe no log mascarado (o próprio tipo do número o mascara), o tipo e o
 * valor cobrado, que é o que saiu do saldo. O valor ao lado do número completo do cartão seria um
 * dado que ninguém deveria juntar no log, e por isso o número nunca é completo. O estabelecimento e
 * o produto ficam de fora: são texto livre do cliente.
 */
public final class CompraRegistrada {

    private CompraRegistrada() {
    }

    /**
     * Registra a compra.
     *
     * @param saida       a porta de log
     * @param comprovante o comprovante da compra efetivada
     */
    public static void registrar(SaidaDeLogPorta saida, DtoComprovanteCompra comprovante) {
        saida.informacao("compra.registrada", campo("cartao", comprovante.numeroCartao()),
                campo("tipoCartao", comprovante.tipo()),
                campo("valorCobrado", comprovante.valorCobrado().valor().toPlainString()));
    }
}
