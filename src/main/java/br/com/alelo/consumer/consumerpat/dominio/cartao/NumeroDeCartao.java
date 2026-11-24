package br.com.alelo.consumer.consumerpat.dominio.cartao;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * O número de um cartão: de {@value #MINIMO_DE_DIGITOS} a {@value #MAXIMO_DE_DIGITOS} dígitos, sem
 * separador.
 *
 * <p>
 * Guarda a forma canônica, e é por isso que dois cartões com o mesmo número são o mesmo cartão:
 * {@code 5555 0000 1111 2222} e {@code 5555000011112222} são um número só, e uma restrição de
 * unicidade do banco só funciona se todo número passa pela mesma normalização. Este construtor é
 * para código que já tem o número normalizado; o texto vindo de fora passa por
 * {@link NumeroDeCartaoDeTexto}, que o normaliza e valida sem lançar exceção.
 *
 * <p>
 * É um dado sensível. O {@code toString()} gerado por um {@code record} imprimiria o número por
 * inteiro em qualquer log, então é sobrescrito e devolve só a forma mascarada
 * ({@link Mascaravel#mascarado()}): nenhum {@code log.info("{}", cartao)} o vaza.
 *
 * @param digitos os dígitos do número, já normalizados
 */
public record NumeroDeCartao(String digitos) implements Mascaravel {

    /** Menor quantidade de dígitos de um número de cartão. */
    public static final int MINIMO_DE_DIGITOS = 13;

    /** Maior quantidade de dígitos de um número de cartão. */
    public static final int MAXIMO_DE_DIGITOS = 19;

    private static final Pattern FORMATO = Pattern
            .compile("\\d{" + MINIMO_DE_DIGITOS + "," + MAXIMO_DE_DIGITOS + "}");

    /**
     * Cria o número a partir dos dígitos já normalizados.
     *
     * <p>
     * Um número fora do formato é erro de quem construiu, e lança em vez de seguir adiante. A
     * mensagem da exceção não inclui o valor recebido, porque ele seria um número de cartão.
     *
     * @param digitos os dígitos, sem separador
     * @throws IllegalArgumentException se não são de 13 a 19 dígitos
     */
    public NumeroDeCartao {
        digitos = Optional.ofNullable(digitos).filter(FORMATO.asMatchPredicate())
                .orElseThrow(() -> new IllegalArgumentException("O número do cartão precisa ter de "
                        + MINIMO_DE_DIGITOS + " a " + MAXIMO_DE_DIGITOS + " dígitos"));
    }

    @Override
    public String toString() {
        return mascarado();
    }
}
