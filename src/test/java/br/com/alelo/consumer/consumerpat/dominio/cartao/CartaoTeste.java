package br.com.alelo.consumer.consumerpat.dominio.cartao;

import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.erros.regradenegocio.ErroSaldoInsuficiente;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroValorNaoPositivo;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CartaoTeste {

    private static final NumeroDeCartao NUMERO = new NumeroDeCartao("5555000011112222");

    private static Dinheiro dinheiro(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    private static Cartao cartaoComSaldo(String saldo) {
        return new Cartao(NUMERO, TipoCartao.ALIMENTACAO, dinheiro(saldo));
    }

    @Test
    void deve_diminuir_o_saldo_quando_debitar_valor_menor_que_o_saldo() {
        // Preparar
        var cartao = cartaoComSaldo("100.00");

        // Agir
        var resultado = cartao.debitar(dinheiro("30.00"));

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isEqualTo(cartaoComSaldo("70.00"));
    }

    @Test
    void deve_zerar_o_saldo_quando_debitar_exatamente_o_saldo() {
        // Preparar
        var cartao = cartaoComSaldo("100.00");

        // Agir
        var resultado = cartao.debitar(dinheiro("100.00"));

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isEqualTo(cartaoComSaldo("0.00"));
    }

    @Test
    void deve_guardar_a_fracao_de_centavo_quando_debitar_valor_com_mais_de_duas_casas() {
        // Preparar
        var cartao = cartaoComSaldo("100.00");

        // Agir
        var resultado = cartao.debitar(dinheiro("29.997"));

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isEqualTo(cartaoComSaldo("70.003"));
    }

    @Test
    void deve_recusar_com_saldo_insuficiente_quando_debitar_um_milionesimo_a_mais_que_o_saldo() {
        // Preparar
        var cartao = cartaoComSaldo("100.00");
        var excedente = dinheiro("100.000001");

        // Agir
        var resultado = cartao.debitar(excedente);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        var erro = assertInstanceOf(ErroSaldoInsuficiente.class, falha.erro());
        assertThat(erro.saldo()).isEqualTo(dinheiro("100.00"));
        assertThat(erro.valor()).isEqualTo(excedente);
    }

    @Test
    void deve_manter_o_saldo_quando_o_debito_e_recusado() {
        // Preparar
        var cartao = cartaoComSaldo("10.00");

        // Agir
        cartao.debitar(dinheiro("50.00"));

        // Verificar
        assertThat(cartao.saldo()).isEqualTo(dinheiro("10.00"));
    }

    @Test
    void deve_manter_o_cartao_original_quando_o_debito_tem_sucesso() {
        // Preparar
        var cartao = cartaoComSaldo("100.00");

        // Agir
        cartao.debitar(dinheiro("30.00"));

        // Verificar
        assertThat(cartao.saldo()).isEqualTo(dinheiro("100.00"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "0.00", "-0.000001", "-50.00" })
    void deve_recusar_valor_nao_positivo_quando_debitar(String valor) {
        // Preparar
        var cartao = cartaoComSaldo("100.00");

        // Agir
        var resultado = cartao.debitar(dinheiro(valor));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroValorNaoPositivo.class);
    }

    @Test
    void deve_recusar_valor_nao_positivo_antes_de_conferir_o_saldo_quando_debitar_negativo() {
        // Preparar
        var cartao = cartaoComSaldo("0.00");

        // Agir
        var resultado = cartao.debitar(dinheiro("-1.00"));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroValorNaoPositivo.class);
    }

    @Test
    void deve_aumentar_o_saldo_quando_creditar_valor_positivo() {
        // Preparar
        var cartao = cartaoComSaldo("50.00");

        // Agir
        var resultado = cartao.creditar(dinheiro("25.50"));

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isEqualTo(cartaoComSaldo("75.50"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "0.00", "-0.000001", "-50.00" })
    void deve_recusar_valor_nao_positivo_quando_creditar(String valor) {
        // Preparar
        var cartao = cartaoComSaldo("50.00");

        // Agir
        var resultado = cartao.creditar(dinheiro(valor));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroValorNaoPositivo.class);
    }

    @Test
    void deve_recusar_valor_nulo_quando_debitar_ou_creditar() {
        // Preparar
        var cartao = cartaoComSaldo("50.00");

        // Verificar
        assertThat(cartao.debitar(null)).isInstanceOf(Falha.class);
        assertThat(cartao.creditar(null)).isInstanceOf(Falha.class);
    }

    @Test
    void deve_manter_numero_e_tipo_quando_o_saldo_muda() {
        // Preparar
        var cartao = new Cartao(NUMERO, TipoCartao.COMBUSTIVEL, dinheiro("10.00"));

        // Agir
        var novo = cartao.comSaldo(dinheiro("99.00"));

        // Verificar
        assertThat(novo.numero()).isEqualTo(NUMERO);
        assertThat(novo.tipo()).isEqualTo(TipoCartao.COMBUSTIVEL);
        assertThat(novo.saldo()).isEqualTo(dinheiro("99.00"));
    }

    @Test
    void deve_aceitar_saldo_zero_quando_criar() {
        // Agir
        var cartao = cartaoComSaldo("0.00");

        // Verificar
        assertThat(cartao.saldo()).isEqualTo(Dinheiro.ZERO);
    }

    @Test
    void deve_lancar_quando_o_saldo_e_negativo() {
        // Agir e Verificar
        assertThatThrownBy(() -> cartaoComSaldo("-0.01"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @NullSource
    void deve_lancar_quando_o_saldo_e_nulo(Dinheiro saldo) {
        // Agir e Verificar
        assertThatThrownBy(() -> new Cartao(NUMERO, TipoCartao.FARMACIA, saldo))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_lancar_quando_o_numero_ou_o_tipo_e_nulo() {
        // Agir e Verificar
        assertThatThrownBy(() -> new Cartao(null, TipoCartao.FARMACIA, Dinheiro.ZERO))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Cartao(NUMERO, null, Dinheiro.ZERO))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deve_nao_revelar_o_numero_quando_converter_em_texto() {
        // Preparar
        var cartao = cartaoComSaldo("10.00");

        // Agir
        var texto = cartao.toString();

        // Verificar
        assertThat(texto).contains("****2222").doesNotContain("5555000011112222");
    }
}
