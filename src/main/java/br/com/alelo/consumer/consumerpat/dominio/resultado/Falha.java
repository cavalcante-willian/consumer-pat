package br.com.alelo.consumer.consumerpat.dominio.resultado;

import java.util.function.Function;

/**
 * Variante de {@link Resultado} em que a operação falhou e há um erro.
 *
 * <p>
 * Implementa uma única operação, {@link #resolver}: as demais ({@link
 * Transformavel#mapear} e {@link Encadeavel#encadear}) são escritas em cima
 * dela, cada uma em seu arquivo. Transformar ou encadear uma falha não executa
 * a função recebida: o erro atravessa a cadeia intacto, e é isso que faz a
 * primeira falha ser a que chega a quem chamou.
 *
 * @param erro erro que explica a falha
 * @param <T>  tipo do valor que a outra variante carregaria
 * @param <E>  tipo do erro
 */
public record Falha<T, E>(E erro) implements Resultado<T, E> {

    @Override
    public <R> R resolver(Function<? super T, ? extends R> aoSucesso, Function<? super E, ? extends R> aoFalhar) {
        return aoFalhar.apply(erro);
    }
}
