package br.com.alelo.consumer.consumerpat.dominio.resultado;

import java.util.function.Function;

/**
 * Papel de quem sabe encadear uma segunda operação que também pode falhar.
 *
 * <p>
 * É o que deixa o fluxo em linha reta: a segunda operação só roda se a primeira teve sucesso, e a
 * primeira falha interrompe a cadeia e chega a quem chamou, sem que as etapas seguintes executem e
 * sem {@code try}/{@code catch} no caminho.
 *
 * <p>
 * É escrita só em cima de {@link #resolver}, a operação-base. Por isso as variantes não a
 * implementam: o encadeamento vive inteiro neste arquivo.
 *
 * @param <T> tipo do valor, quando há sucesso
 * @param <E> tipo do erro, quando há falha
 */
public interface Encadeavel<T, E> extends Resolvivel<T, E> {

    /**
     * Roda a operação seguinte com o valor, se houver, ou devolve a mesma falha.
     *
     * @param funcao operação seguinte, aplicada só ao valor de um sucesso
     * @param <U>    tipo do valor da operação seguinte
     * @return o resultado da operação seguinte, ou a mesma falha
     */
    default <U> Resultado<U, E> encadear(Function<? super T, Resultado<U, E>> funcao) {
        return this.<Resultado<U, E>>resolver(valor -> funcao.apply(valor),
                erro -> new Falha<>(erro));
    }
}
