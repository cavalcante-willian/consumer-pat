package br.com.alelo.consumer.consumerpat.dominio.dinheiro;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Apresenta uma quantia em centavos.
 *
 * <p>
 * Fica fora do {@link Dinheiro} porque a apresentação muda por razões que o
 * valor não tem: o
 * modo de arredondar é decisão de quem mostra, e não de quem guarda. O modo é
 * obrigatório, sem
 * padrão, porque {@code HALF_UP} e {@code HALF_EVEN} dão resultados diferentes
 * em 29,985, e deixar
 * um deles implícito seria decidir sem perceber.
 */
public final class ApresentacaoDeDinheiro {

    private static final int CASAS_DE_CENTAVOS = 2;

    private ApresentacaoDeDinheiro() {
    }

    /**
     * A quantia com duas casas decimais.
     *
     * @param dinheiro a quantia exata, com a fração de centavo
     * @param modo     como arredondar a fração de centavo
     * @return a quantia em centavos
     */
    public static BigDecimal emCentavos(Dinheiro dinheiro, RoundingMode modo) {
        return dinheiro.valor().setScale(CASAS_DE_CENTAVOS, modo);
    }
}
