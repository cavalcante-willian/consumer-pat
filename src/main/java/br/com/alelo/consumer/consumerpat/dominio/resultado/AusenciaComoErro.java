package br.com.alelo.consumer.consumerpat.dominio.resultado;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Dá nome de erro à ausência de um valor.
 *
 * <p>
 * É a fronteira entre código que usa {@link Optional}, como um repositório, e o domínio, onde a
 * ausência precisa ser um erro tipado e nunca um {@code null} que atravessa. Fica fora do
 * {@link Resultado} porque converter uma origem de ausência é outro motivo de mudar: uma nova
 * origem (um valor nulo, uma exceção) vira outro conversor, sem tocar no tipo do resultado.
 */
public final class AusenciaComoErro {

    private AusenciaComoErro() {
    }

    /**
     * Converte um valor possivelmente ausente em resultado.
     *
     * @param opcional      valor possivelmente ausente
     * @param erroSeAusente fornece o erro quando o valor está ausente; só é chamado nesse caso
     * @param <T>           tipo do valor
     * @param <E>           tipo do erro
     * @return sucesso com o valor presente, ou falha com o erro fornecido
     */
    public static <T, E> Resultado<T, E> converter(Optional<T> opcional,
            Supplier<E> erroSeAusente) {
        return opcional.<Resultado<T, E>>map(valor -> new Sucesso<>(valor))
                .orElseGet(() -> new Falha<>(erroSeAusente.get()));
    }
}
