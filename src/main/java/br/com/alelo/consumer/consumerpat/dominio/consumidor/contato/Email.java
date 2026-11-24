package br.com.alelo.consumer.consumerpat.dominio.consumidor.contato;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Um endereço de e-mail: até {@value #MAXIMO_DE_CARACTERES} caracteres, no formato
 * {@code nome@dominio.sufixo}.
 *
 * <p>
 * A conferência é só de forma, a que impede lixo óbvio: se o endereço existe só se sabe enviando
 * uma mensagem. É um dado pessoal, e o {@code toString()} esconde o nome e mostra só o domínio.
 *
 * @param endereco o endereço, sem espaço nas pontas
 */
public record Email(String endereco) {

    /** Maior quantidade de caracteres de um endereço. */
    public static final int MAXIMO_DE_CARACTERES = 254;

    /** O formato aceito, com o limite de tamanho; compartilhado com a leitura do texto. */
    public static final Pattern FORMATO = Pattern
            .compile("(?=.{1," + MAXIMO_DE_CARACTERES + "}$)[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+");

    /**
     * Cria o e-mail a partir do endereço.
     *
     * @param endereco o endereço
     * @throws IllegalArgumentException se não tem o formato esperado; a mensagem não traz o valor
     */
    public Email {
        endereco = Optional.ofNullable(endereco).map(String::strip)
                .filter(FORMATO.asMatchPredicate())
                .orElseThrow(() -> new IllegalArgumentException(
                        "O e-mail precisa ter o formato nome@dominio.sufixo e até "
                                + MAXIMO_DE_CARACTERES + " caracteres"));
    }

    @Override
    public String toString() {
        return "***" + endereco.substring(endereco.indexOf('@'));
    }
}
