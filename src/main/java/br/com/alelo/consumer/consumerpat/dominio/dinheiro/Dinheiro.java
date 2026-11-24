package br.com.alelo.consumer.consumerpat.dominio.dinheiro;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Quantia em reais, sempre com {@value #ESCALA} casas decimais.
 *
 * <p>
 * Guarda a fração de centavo que os cálculos produzem: 10% de desconto sobre
 * 33,33 é 29,997, e arredondar a cada operação descartaria a terceira casa e
 * acumularia erro. Este tipo só guarda a quantia e garante a escala; cada
 * operação sobre ela é um papel em arquivo próprio ({@link Somavel},
 * {@link Subtraivel}, {@link Multiplicavel} e {@link ComparavelPorQuantia}),
 * assim como ler uma quantia de texto vindo de fora ({@link DinheiroDeTexto}) e
 * apresentá-la em centavos ({@link ApresentacaoDeDinheiro}).
 *
 * <p>
 * Toda instância tem a mesma escala, então {@code equals} funciona como se
 * espera: 2,0 e 2,00 são a mesma quantia. Com {@link BigDecimal} solto isso é
 * falso, porque ele compara a escala junto com o valor.
 *
 * @param valor a quantia, já na escala de {@value #ESCALA} casas
 */
public record Dinheiro(BigDecimal valor) implements Somavel, Subtraivel, Multiplicavel, ComparavelPorQuantia {

    /**
     * Casas decimais guardadas. Deve ser igual à escala da coluna do banco que
     * guarda dinheiro: se divergirem, o banco arredonda ou trunca em silêncio ao
     * gravar.
     */
    public static final int ESCALA = 6;

    /**
     * Total de dígitos guardados, com {@value #ESCALA} casas: o que cabe em
     * {@code NUMERIC(19,6)}.
     */
    public static final int PRECISAO = 19;

    /** Quantia nula. */
    public static final Dinheiro ZERO = new Dinheiro(BigDecimal.ZERO);

    /**
     * Cria a quantia na escala de {@value #ESCALA} casas.
     *
     * <p>
     * Este construtor é para código que já tem o valor confiável. Se o valor
     * tiver mais casas do que a escala, perderia dígitos, e isso é erro de quem
     * construiu: lança {@link ArithmeticException} em vez de arredondar sem
     * avisar. Dado vindo de fora passa por {@link DinheiroDeTexto}, que valida
     * antes.
     *
     * @param valor a quantia
     * @throws ArithmeticException se o valor tem mais casas decimais que a escala
     */
    public Dinheiro {
        valor = valor.setScale(ESCALA, RoundingMode.UNNECESSARY);
    }
}
