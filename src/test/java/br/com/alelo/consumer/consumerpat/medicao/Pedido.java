package br.com.alelo.consumer.consumerpat.medicao;

import java.util.concurrent.Callable;

/**
 * Uma chamada que se mede: o que ela é, para o relatório juntar o legado e o novo na mesma linha, e
 * como executá-la.
 *
 * @param chave     o nome estável da chamada, igual nos dois sistemas
 * @param descricao o que a chamada faz neste sistema, na rota e no formato dele
 * @param chamada   a chamada
 */
public record Pedido(String chave, String descricao, Callable<RespostaHttp> chamada) {
}
