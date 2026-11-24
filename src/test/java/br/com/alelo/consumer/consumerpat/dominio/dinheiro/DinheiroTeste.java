package br.com.alelo.consumer.consumerpat.dominio.dinheiro;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DinheiroTeste {

    private static Dinheiro dinheiro(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    @Test
    void deve_guardar_a_fracao_quando_multiplicar_por_fator() {
        // Preparar
        var valor = dinheiro("33.33");

        // Agir
        var cobrado = valor.vezes(new BigDecimal("0.90"));

        // Verificar
        assertThat(cobrado).isEqualTo(dinheiro("29.997"));
    }

    @Test
    void deve_somar_exato_quando_acumular_fracoes() {
        // Preparar
        var umDecimo = dinheiro("0.1");
        var doisDecimos = dinheiro("0.2");

        // Agir
        var soma = umDecimo.mais(doisDecimos);

        // Verificar
        assertThat(soma).isEqualTo(dinheiro("0.3"));
    }

    @Test
    void deve_subtrair_exato_quando_o_resultado_e_negativo() {
        // Preparar
        var saldo = dinheiro("10");

        // Agir
        var diferenca = saldo.menos(dinheiro("12.5"));

        // Verificar
        assertThat(diferenca).isEqualTo(dinheiro("-2.5"));
    }

    @Test
    void deve_lancar_quando_construir_com_mais_casas_que_a_escala() {
        // Preparar
        var excessoDeCasas = new BigDecimal("0.0000001");

        // Agir e Verificar
        assertThatThrownBy(() -> new Dinheiro(excessoDeCasas)).isInstanceOf(ArithmeticException.class);
    }

    @Test
    void deve_arredondar_para_o_par_na_escala_interna_quando_a_multiplicacao_excede_a_escala() {
        // Preparar
        var metadeParaBaixo = dinheiro("0.000001");
        var metadeParaCima = dinheiro("0.000003");
        var meio = new BigDecimal("0.5");

        // Agir
        var arredondadoParaZero = metadeParaBaixo.vezes(meio);
        var arredondadoParaDois = metadeParaCima.vezes(meio);

        // Verificar
        assertThat(arredondadoParaZero).isEqualTo(Dinheiro.ZERO);
        assertThat(arredondadoParaDois).isEqualTo(dinheiro("0.000002"));
    }

    @Test
    void deve_ser_igual_quando_a_mesma_quantia_chega_com_escalas_diferentes() {
        // Preparar
        var comUmaCasa = dinheiro("2.0");
        var comDuasCasas = dinheiro("2.00");

        // Verificar
        assertThat(comUmaCasa).isEqualTo(comDuasCasas).hasSameHashCodeAs(comDuasCasas);
    }

    @Test
    void deve_ordenar_pela_quantia_quando_comparar() {
        // Preparar
        var menor = dinheiro("1.5");
        var maior = dinheiro("1.50001");

        // Verificar
        assertThat(menor).isLessThan(maior);
        assertThat(maior).isGreaterThan(menor);
        assertThat(menor).isEqualByComparingTo(dinheiro("1.500000"));
    }

    @Test
    void deve_ter_o_valor_zero_na_escala_interna_quando_ler_a_constante() {
        // Verificar
        assertThat(Dinheiro.ZERO.valor()).isEqualTo(new BigDecimal("0.000000"));
    }
}
