package br.com.alelo.consumer.consumerpat.medicao;

import java.util.List;

/**
 * Um ponto da base de consumidores onde se mede: o início, o meio e o fim.
 *
 * <p>
 * No legado a busca por cartão varre a tabela, então o consumidor do fim custa mais que o do
 * início, e é essa diferença que as três posições expõem. No novo o número tem índice, e as três
 * devem custar o mesmo.
 *
 * @param nome   o nome da posição, sem acento, usado na chave do pedido
 * @param rotulo o nome da posição como se lê, com acento, usado na descrição
 * @param indice o índice do consumidor, de zero até o total menos um
 */
public record PosicaoNaBase(String nome, String rotulo, int indice) {

    /**
     * As três posições de uma base.
     *
     * @param total quantos consumidores a base tem
     * @return o início, o meio e o fim
     */
    public static List<PosicaoNaBase> de(int total) {
        return List.of(new PosicaoNaBase("inicio", "início", 0),
                new PosicaoNaBase("meio", "meio", total / 2),
                new PosicaoNaBase("fim", "fim", total - 1));
    }
}
