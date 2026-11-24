package br.com.alelo.consumer.consumerpat.dominio.dinheiro;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Papel de uma quantia que sabe multiplicar-se por um fator.
 *
 * <p>
 * É a única operação que pode exceder a escala, e por isso é a única que
 * arredonda, uma vez, para a escala interna, com
 * {@link RoundingMode#HALF_EVEN},
 * que não tem viés em muitas operações.
 */
public interface Multiplicavel extends ComQuantia {

    /**
     * Multiplica por um fator em fração, como 0,90 para um desconto de 10% ou
     * 1,35 para um acréscimo de 35%.
     *
     * @param fator o fator, em fração (nunca em porcentagem: 10% é 0,10, e não
     *              10)
     * @return a quantia multiplicada
     */
    default Dinheiro vezes(BigDecimal fator) {
        return new Dinheiro(valor().multiply(fator).setScale(Dinheiro.ESCALA, RoundingMode.HALF_EVEN));
    }
}
