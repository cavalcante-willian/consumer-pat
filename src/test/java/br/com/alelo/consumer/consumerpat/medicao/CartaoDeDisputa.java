package br.com.alelo.consumer.consumerpat.medicao;

/**
 * Um cartão de farmácia preparado para a disputa simultânea.
 *
 * @param numero     o número do cartão, como o sistema o conhece
 * @param referencia onde o sistema devolve o saldo: o caminho do consumidor no novo, e o próprio
 *                   número no legado, que só lista
 */
public record CartaoDeDisputa(String numero, String referencia) {
}
