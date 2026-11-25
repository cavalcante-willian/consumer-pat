package br.com.alelo.consumer.consumerpat.api.comum.tratamento;

import org.hibernate.exception.ConstraintViolationException;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Extrai de uma violação de integridade o nome da restrição, e nunca o valor que colidiu.
 *
 * <p>
 * A mensagem de uma {@code DataIntegrityViolationException} inclui o valor: o H2 escreve a cláusula
 * {@code VALUES} com o número do cartão ou o documento. O log leva só o nome da restrição, e mesmo
 * dele fica só o primeiro termo, de letras, dígitos, sublinhado e ponto, porque o nome que o driver
 * devolve pode vir com a descrição do índice e dos valores atrás. A regra de não logar dado
 * sensível prevalece sobre a de registrar a causa inteira.
 */
public final class NomeDaRestricao {

    /** O que se registra quando a exceção não traz o nome de nenhuma restrição. */
    public static final String DESCONHECIDA = "desconhecida";

    private static final Pattern PRIMEIRO_TERMO = Pattern.compile("[A-Za-z0-9_.]+");

    private NomeDaRestricao() {
    }

    /**
     * Procura o nome da restrição na cadeia de causas.
     *
     * @param excecao a violação de integridade
     * @return o primeiro termo do nome da restrição, ou {@value #DESCONHECIDA}
     */
    public static String de(Throwable excecao) {
        return Stream.iterate(excecao, Objects::nonNull, Throwable::getCause)
                .filter(ConstraintViolationException.class::isInstance)
                .map(ConstraintViolationException.class::cast)
                .map(ConstraintViolationException::getConstraintName).filter(Objects::nonNull)
                .map(PRIMEIRO_TERMO::matcher).filter(Matcher::find).map(Matcher::group).findFirst()
                .orElse(DESCONHECIDA);
    }
}
