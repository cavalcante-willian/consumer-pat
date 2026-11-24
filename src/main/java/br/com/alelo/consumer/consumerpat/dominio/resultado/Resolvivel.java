package br.com.alelo.consumer.consumerpat.dominio.resultado;

import java.util.function.Function;

/**
 * Papel de quem sabe se converter num único valor, escolhendo o caminho do
 * sucesso ou o da falha.
 *
 * <p>
 * É a saída de uma cadeia de {@link Resultado}: o ponto em que o tipo deixa de
 * carregar a
 * possibilidade de falha e vira, por exemplo, uma resposta. Cada variante sabe
 * qual função usar, então
 * quem chama não decide com {@code if} em que caso está.
 *
 * @param <T> tipo do valor, quando há sucesso
 * @param <E> tipo do erro, quando há falha
 */
public interface Resolvivel<T, E> {

    /**
     * Converte no valor produzido pela função da variante em que se está.
     *
     * @param aoSucesso função aplicada ao valor, se houve sucesso
     * @param aoFalhar  função aplicada ao erro, se houve falha
     * @param <R>       tipo comum devolvido pelas duas funções
     * @return o valor devolvido pela função da variante
     */
    <R> R resolver(Function<? super T, ? extends R> aoSucesso, Function<? super E, ? extends R> aoFalhar);
}
