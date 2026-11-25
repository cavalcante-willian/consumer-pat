package br.com.alelo.consumer.consumerpat.dominio.resultado;

/**
 * Resultado de uma operação que pode falhar: ou um {@link Sucesso} com o valor, ou uma
 * {@link Falha} com o erro.
 *
 * <p>
 * Existe para que a falha conste da assinatura. Um método que devolve {@code null} ou lança exceção
 * obriga quem chama a abrir o corpo para descobrir que pode falhar; com {@code
 * Resultado<T, E>} o tipo já diz.
 *
 * <p>
 * O que se faz com um resultado vem dos papéis que ele compõe, cada um em seu tipo:
 * {@link Resolvivel}, a operação-base, {@link Transformavel}, {@link Encadeavel},
 * {@link ObservavelNoSucesso} e {@link ObservavelNaFalha}. Só há duas variantes, e o compilador as
 * conhece: a interface é selada. Para criar um resultado usa-se o construtor da variante,
 * {@code new Sucesso<>(valor)} ou {@code new Falha<>(erro)}.
 *
 * @param <T> tipo do valor, quando a operação tem sucesso
 * @param <E> tipo do erro, quando a operação falha
 */
public sealed interface Resultado<T, E> extends Transformavel<T, E>, Encadeavel<T, E>,
        ObservavelNoSucesso<T, E>, ObservavelNaFalha<T, E> permits Sucesso, Falha {
}
