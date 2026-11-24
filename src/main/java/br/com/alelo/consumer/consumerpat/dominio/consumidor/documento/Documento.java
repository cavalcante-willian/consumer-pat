package br.com.alelo.consumer.consumerpat.dominio.consumidor.documento;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Mascaravel;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * O documento de um consumidor: {@value #DIGITOS_DE_CPF} dígitos (CPF) ou {@value #DIGITOS_DE_CNPJ}
 * (CNPJ), sem pontuação.
 *
 * <p>
 * Guarda a forma canônica, e é por isso que dois cadastros do mesmo documento são o mesmo
 * documento: {@code 123.456.789-01} e {@code 12345678901} são um só, e a unicidade do banco só
 * funciona se toda escrita passa pela mesma normalização. Este construtor é para código que já tem
 * o documento normalizado; o texto vindo de fora passa por {@link DocumentoDeTexto}. Os dígitos
 * verificadores não são conferidos: só o formato.
 *
 * <p>
 * É um dado pessoal. O {@code toString()} gerado por um {@code record} o imprimiria por inteiro em
 * qualquer log, então é sobrescrito e devolve só a forma mascarada.
 *
 * @param digitos os dígitos do documento, já normalizados
 */
public record Documento(String digitos) implements Mascaravel {

    /** Quantidade de dígitos de um CPF. */
    public static final int DIGITOS_DE_CPF = 11;

    /** Quantidade de dígitos de um CNPJ. */
    public static final int DIGITOS_DE_CNPJ = 14;

    private static final Pattern FORMATO = Pattern
            .compile("\\d{" + DIGITOS_DE_CPF + "}|\\d{" + DIGITOS_DE_CNPJ + "}");

    /**
     * Cria o documento a partir dos dígitos já normalizados.
     *
     * <p>
     * Um documento fora do formato é erro de quem construiu, e lança. A mensagem da exceção não
     * inclui o valor recebido.
     *
     * @param digitos os dígitos, sem pontuação
     * @throws IllegalArgumentException se não são 11 nem 14 dígitos
     */
    public Documento {
        digitos = Optional.ofNullable(digitos).filter(FORMATO.asMatchPredicate())
                .orElseThrow(() -> new IllegalArgumentException("O documento precisa ter "
                        + DIGITOS_DE_CPF + " ou " + DIGITOS_DE_CNPJ + " dígitos"));
    }

    @Override
    public String toString() {
        return mascarado();
    }
}
