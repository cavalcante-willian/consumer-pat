package br.com.alelo.consumer.consumerpat.dominio.erros.regradenegocio;

import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ErroSaldoInsuficienteTeste {

    private static final Dinheiro SALDO = new Dinheiro(new BigDecimal("123.45"));

    private static final Dinheiro VALOR = new Dinheiro(new BigDecimal("999.99"));

    @Test
    void deve_ser_da_categoria_regra_de_negocio_quando_consultar_a_categoria() {
        // Preparar
        var erro = new ErroSaldoInsuficiente(SALDO, VALOR);

        // Verificar
        assertThat(erro.categoria()).isEqualTo(CategoriaErro.REGRA_DE_NEGOCIO);
        assertThat(erro.categoria().statusHttp()).isEqualTo(422);
    }

    @Test
    void deve_expor_o_codigo_da_constante_quando_consultar_o_codigo() {
        // Preparar
        var erro = new ErroSaldoInsuficiente(SALDO, VALOR);

        // Verificar
        assertThat(erro.codigo()).isEqualTo(ErroSaldoInsuficiente.CODIGO);
    }

    @Test
    void deve_guardar_o_saldo_e_o_valor_para_o_log_quando_criar() {
        // Preparar
        var erro = new ErroSaldoInsuficiente(SALDO, VALOR);

        // Verificar
        assertThat(erro.saldo()).isEqualTo(SALDO);
        assertThat(erro.valor()).isEqualTo(VALOR);
    }

    @Test
    void deve_nao_repetir_o_saldo_nem_o_valor_quando_consultar_a_mensagem() {
        // Preparar
        var erro = new ErroSaldoInsuficiente(SALDO, VALOR);

        // Verificar
        assertThat(erro.mensagem()).doesNotContain("123").doesNotContain("999");
    }

    @Test
    void deve_nao_apontar_campo_quando_consultar_os_campos_com_erro() {
        // Preparar
        var erro = new ErroSaldoInsuficiente(SALDO, VALOR);

        // Verificar
        assertThat(erro.camposComErro()).isEmpty();
    }
}
