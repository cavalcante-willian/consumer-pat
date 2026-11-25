package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.compra;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoCompra;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.log.SaidaDeLogPorta;

import static br.com.alelo.consumer.consumerpat.dominio.log.CampoDeLog.campo;

/**
 * Registra que uma compra foi recusada: o evento {@code compra.recusada}.
 *
 * <p>
 * Uma recusa de negócio, como o saldo insuficiente ou o cartão que não existe, é resultado
 * esperado, e não falha do sistema: o nível é informação, e não erro. O evento leva o código do
 * erro como motivo, que é o que liga a linha do log ao {@code type} da resposta, e nunca a mensagem
 * do erro. O cartão sai mascarado.
 */
public final class CompraRecusada {

    private CompraRecusada() {
    }

    /**
     * Registra a recusa.
     *
     * @param saida  a porta de log
     * @param compra o pedido que foi recusado
     * @param erro   o erro que o domínio devolveu
     */
    public static void registrar(SaidaDeLogPorta saida, DtoCompra compra, ErroDominio erro) {
        saida.informacao("compra.recusada", campo("cartao", compra.numeroCartao()),
                campo("tipoCartao", compra.tipo()), campo("motivo", erro.codigo()));
    }
}
