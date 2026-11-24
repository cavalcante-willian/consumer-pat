package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoCompra;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.extrato.Extrato;

import java.time.Instant;

/**
 * Monta o extrato de uma compra a partir do pedido.
 *
 * <p>
 * É a única peça que sabe como o pedido vira registro: se o extrato ganhar um campo, muda aqui, e
 * não no caso de uso. É pura, e por isso o instante entra como argumento em vez de ser lido de um
 * relógio. Construir o extrato antes de qualquer gravação faz um pedido incompleto, com um texto
 * nulo, falhar antes de travar ou gravar cartão.
 */
public final class ExtratoDaCompra {

    private ExtratoDaCompra() {
    }

    /**
     * Monta o extrato.
     *
     * @param compra   o pedido
     * @param cobrado  o valor debitado, já com o ajuste do tipo
     * @param instante o instante da compra
     * @return o extrato, com o valor informado e o cobrado
     * @throws NullPointerException se algum campo do pedido é nulo
     */
    public static Extrato montar(DtoCompra compra, Dinheiro cobrado, Instant instante) {
        return new Extrato(compra.nomeEstabelecimento(), compra.descricaoProduto(), instante,
                compra.numeroCartao(), compra.tipo(), compra.valor(), cobrado);
    }
}
