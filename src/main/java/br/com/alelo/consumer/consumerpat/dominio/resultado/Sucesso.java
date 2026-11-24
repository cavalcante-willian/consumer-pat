package br.com.alelo.consumer.consumerpat.dominio.resultado;

import java.util.function.Function;

/**
 * Variante de {@link Resultado} em que a operação teve sucesso e há um valor.
 *
 * <p>
 * Implementa uma única operação, {@link #resolver}: as demais ({@link Transformavel#mapear} e
 * {@link Encadeavel#encadear}) são escritas em cima dela, cada uma em seu arquivo.
 *
 * @param valor valor produzido pela operação
 * @param <T>   tipo do valor
 * @param <E>   tipo do erro que a outra variante carregaria
 */
public record Sucesso<T, E>(T valor) implements Resultado<T, E> {

    @Override
    public <R> R resolver(Function<? super T, ? extends R> aoSucesso,
            Function<? super E, ? extends R> aoFalhar) {
        return aoSucesso.apply(valor);
    }
}
