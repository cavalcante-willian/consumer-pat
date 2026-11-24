package br.com.alelo.consumer.consumerpat.dominio.resultado;

import java.util.function.Function;

/**
 * Papel de quem sabe transformar o valor de um sucesso sem tocar numa falha.
 *
 * <p>
 * A transformação nunca pode falhar: se pode, a operação seguinte é um {@link Encadeavel#encadear
 * encadeamento}, e não uma transformação. Numa falha a função não executa e o erro segue adiante.
 *
 * <p>
 * É escrita só em cima de {@link #resolver}, a operação-base. Por isso as variantes não a
 * implementam: a transformação vive inteira neste arquivo.
 *
 * @param <T> tipo do valor, quando há sucesso
 * @param <E> tipo do erro, quando há falha
 */
public interface Transformavel<T, E> extends Resolvivel<T, E> {

    /**
     * Transforma o valor, se houver, e deixa a falha como está.
     *
     * @param funcao transformação aplicada só ao valor de um sucesso
     * @param <U>    tipo do valor transformado
     * @return sucesso com o valor transformado, ou a mesma falha
     */
    default <U> Resultado<U, E> mapear(Function<? super T, ? extends U> funcao) {
        return this.<Resultado<U, E>>resolver(valor -> new Sucesso<>(funcao.apply(valor)),
                erro -> new Falha<>(erro));
    }
}
