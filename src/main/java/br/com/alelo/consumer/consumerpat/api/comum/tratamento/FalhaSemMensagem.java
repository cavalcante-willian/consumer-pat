package br.com.alelo.consumer.consumerpat.api.comum.tratamento;

import java.util.Optional;

/**
 * A pilha de uma falha, sem a mensagem dela.
 *
 * <p>
 * A mensagem de uma exceção inesperada pode citar SQL, um valor vinculado, um número de cartão ou
 * um documento, e nada no log a mascara: a barreira do padrão não alcança a pilha. Este invólucro
 * guarda o que o operador precisa para achar o defeito, o tipo da exceção e cada quadro da pilha,
 * da causa-raiz também, e descarta o que pode vazar. A mensagem do invólucro é o nome da classe da
 * exceção original.
 */
public class FalhaSemMensagem extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Cria o invólucro, e o da causa, recursivamente.
     *
     * @param original a exceção original, cuja mensagem não é copiada
     */
    public FalhaSemMensagem(Throwable original) {
        super(original.getClass().getName(),
                Optional.ofNullable(original.getCause()).map(FalhaSemMensagem::new).orElse(null),
                false, true);
        setStackTrace(original.getStackTrace());
    }
}
