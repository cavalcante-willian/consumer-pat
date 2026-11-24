package br.com.alelo.consumer.consumerpat.dominio.consumidor.endereco;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Um CEP: {@value #DIGITOS} dígitos, sem hífen.
 *
 * <p>
 * Guarda a forma canônica; o texto vindo de fora passa por {@link CepDeTexto}. Um CEP não
 * identifica uma pessoa, então o {@code toString()} gerado pelo {@code record} é mantido.
 *
 * @param digitos os dígitos do CEP, já normalizados
 */
public record Cep(String digitos) {

    /** Quantidade de dígitos de um CEP. */
    public static final int DIGITOS = 8;

    private static final Pattern FORMATO = Pattern.compile("\\d{" + DIGITOS + "}");

    /**
     * Cria o CEP a partir dos dígitos já normalizados.
     *
     * @param digitos os dígitos, sem hífen
     * @throws IllegalArgumentException se não são 8 dígitos
     */
    public Cep {
        digitos = Optional.ofNullable(digitos).filter(FORMATO.asMatchPredicate()).orElseThrow(
                () -> new IllegalArgumentException("O CEP precisa ter " + DIGITOS + " dígitos"));
    }
}
