package br.com.alelo.consumer.consumerpat.dominio.consumidor.contato;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Mascaravel;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Um telefone: de {@value #MINIMO_DE_DIGITOS} a {@value #MAXIMO_DE_DIGITOS} dígitos, com o DDD e
 * sem pontuação.
 *
 * <p>
 * Guarda a forma canônica; o texto vindo de fora passa por {@link TelefoneDeTexto}. É um dado
 * pessoal, e o {@code toString()} devolve só a forma mascarada, para nenhum log o imprimir por
 * inteiro.
 *
 * @param digitos os dígitos do telefone, já normalizados
 */
public record Telefone(String digitos) implements Mascaravel {

    /** Menor quantidade de dígitos de um telefone. */
    public static final int MINIMO_DE_DIGITOS = 10;

    /** Maior quantidade de dígitos de um telefone. */
    public static final int MAXIMO_DE_DIGITOS = 15;

    private static final Pattern FORMATO = Pattern
            .compile("\\d{" + MINIMO_DE_DIGITOS + "," + MAXIMO_DE_DIGITOS + "}");

    /**
     * Cria o telefone a partir dos dígitos já normalizados.
     *
     * @param digitos os dígitos, sem pontuação
     * @throws IllegalArgumentException se não são de 10 a 15 dígitos; a mensagem não traz o valor
     */
    public Telefone {
        digitos = Optional.ofNullable(digitos).filter(FORMATO.asMatchPredicate())
                .orElseThrow(() -> new IllegalArgumentException("O telefone precisa ter de "
                        + MINIMO_DE_DIGITOS + " a " + MAXIMO_DE_DIGITOS + " dígitos"));
    }

    @Override
    public String toString() {
        return mascarado();
    }
}
