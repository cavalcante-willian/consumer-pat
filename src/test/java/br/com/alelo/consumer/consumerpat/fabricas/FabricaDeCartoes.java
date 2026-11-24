package br.com.alelo.consumer.consumerpat.fabricas;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;

/**
 * O ponto de partida de todo cartão de teste: o tipo escolhido, com o número e o saldo padrão.
 *
 * <p>
 * Os padrões dizem só o que o teste quer destacar: o número é sempre o mesmo, a menos que o teste o
 * troque, e o saldo é cem reais.
 */
public final class FabricaDeCartoes {

    /** O número que todo cartão da fábrica tem, a menos que o teste o troque. */
    public static final String NUMERO_PADRAO = "5555000011112222";

    /** O saldo que todo cartão da fábrica tem, a menos que o teste o troque. */
    public static final String SALDO_PADRAO = "100.00";

    private FabricaDeCartoes() {
    }

    /**
     * Começa um cartão do tipo informado.
     *
     * @param tipo o tipo do cartão
     * @return o modelo, com o número e o saldo padrão
     */
    public static ModeloDeCartao doTipo(TipoCartao tipo) {
        return new ModeloDeCartao(tipo, NUMERO_PADRAO, SALDO_PADRAO);
    }
}
