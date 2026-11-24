package br.com.alelo.consumer.consumerpat.dominio.cartao;

import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TipoCartaoTeste {

    private static Dinheiro dinheiro(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    @ParameterizedTest
    @CsvSource({ "ALIMENTACAO, 100.00, 90", "ALIMENTACAO, 33.33, 29.997", "FARMACIA, 100.00, 100",
            "FARMACIA, 33.33, 33.33", "COMBUSTIVEL, 100.00, 135", "COMBUSTIVEL, 33.33, 44.9955" })
    void deve_aplicar_o_ajuste_do_tipo_quando_calcular_o_valor_cobrado(TipoCartao tipo,
            String informado, String esperado) {
        // Agir
        var cobrado = tipo.valorCobrado(dinheiro(informado));

        // Verificar
        assertThat(cobrado).isEqualTo(dinheiro(esperado));
    }

    @Test
    void deve_guardar_a_fracao_de_centavo_quando_calcular_o_desconto_da_alimentacao() {
        // Preparar
        var informado = dinheiro("33.33");

        // Agir
        var cobrado = TipoCartao.ALIMENTACAO.valorCobrado(informado);

        // Verificar
        assertThat(cobrado.valor()).isEqualByComparingTo("29.997");
        assertThat(cobrado).isNotEqualTo(dinheiro("30.00"));
    }

    @ParameterizedTest
    @CsvSource({ "ALIMENTACAO, 1", "FARMACIA, 2", "COMBUSTIVEL, 3" })
    void deve_corresponder_ao_tipo_de_estabelecimento_quando_consultar_o_codigo(TipoCartao tipo,
            int codigo) {
        // Verificar
        assertThat(tipo.codigoDeEstabelecimento()).isEqualTo(codigo);
    }

    @Test
    void deve_ter_so_os_tres_tipos_do_enunciado_quando_listar_os_valores() {
        // Verificar
        assertThat(TipoCartao.values()).containsExactly(TipoCartao.ALIMENTACAO, TipoCartao.FARMACIA,
                TipoCartao.COMBUSTIVEL);
    }

    @Test
    void deve_ter_o_ajuste_em_fracao_com_sinal_quando_consultar_o_ajuste() {
        // Verificar
        assertThat(TipoCartao.ALIMENTACAO.ajusteDoValor()).isEqualByComparingTo("-0.10");
        assertThat(TipoCartao.FARMACIA.ajusteDoValor()).isEqualByComparingTo("0.00");
        assertThat(TipoCartao.COMBUSTIVEL.ajusteDoValor()).isEqualByComparingTo("0.35");
    }
}
