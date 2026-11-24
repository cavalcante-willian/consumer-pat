package br.com.alelo.consumer.consumerpat.dominio.extrato;

import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExtratoTeste {

    private static final Instant INSTANTE = Instant.parse("2026-11-24T12:00:00Z");

    private static final NumeroDeCartao NUMERO = new NumeroDeCartao("5555000011112222");

    private static final Dinheiro INFORMADO = new Dinheiro(new BigDecimal("33.33"));

    private static final Dinheiro COBRADO = new Dinheiro(new BigDecimal("29.997"));

    private static Extrato extrato() {
        return new Extrato("Mercado", "Arroz", INSTANTE, NUMERO, TipoCartao.ALIMENTACAO, INFORMADO,
                COBRADO);
    }

    static Stream<ThrowingCallable> construtoresComUmComponenteNulo() {
        return Stream.of(
                () -> new Extrato(null, "Arroz", INSTANTE, NUMERO, TipoCartao.ALIMENTACAO,
                        INFORMADO, COBRADO),
                () -> new Extrato("Mercado", null, INSTANTE, NUMERO, TipoCartao.ALIMENTACAO,
                        INFORMADO, COBRADO),
                () -> new Extrato("Mercado", "Arroz", null, NUMERO, TipoCartao.ALIMENTACAO,
                        INFORMADO, COBRADO),
                () -> new Extrato("Mercado", "Arroz", INSTANTE, null, TipoCartao.ALIMENTACAO,
                        INFORMADO, COBRADO),
                () -> new Extrato("Mercado", "Arroz", INSTANTE, NUMERO, null, INFORMADO, COBRADO),
                () -> new Extrato("Mercado", "Arroz", INSTANTE, NUMERO, TipoCartao.ALIMENTACAO,
                        null, COBRADO),
                () -> new Extrato("Mercado", "Arroz", INSTANTE, NUMERO, TipoCartao.ALIMENTACAO,
                        INFORMADO, null));
    }

    @Test
    void deve_guardar_o_valor_informado_e_o_cobrado_quando_criar() {
        // Agir
        var extrato = extrato();

        // Verificar
        assertThat(extrato.valorInformado()).isEqualTo(INFORMADO);
        assertThat(extrato.valorCobrado()).isEqualTo(COBRADO);
        assertThat(extrato.valorInformado()).isNotEqualTo(extrato.valorCobrado());
    }

    @Test
    void deve_guardar_os_dados_da_compra_quando_criar() {
        // Agir
        var extrato = extrato();

        // Verificar
        assertThat(extrato.nomeDoEstabelecimento()).isEqualTo("Mercado");
        assertThat(extrato.descricaoDoProduto()).isEqualTo("Arroz");
        assertThat(extrato.dataDaCompra()).isEqualTo(INSTANTE);
        assertThat(extrato.numeroDoCartao()).isEqualTo(NUMERO);
        assertThat(extrato.tipo()).isEqualTo(TipoCartao.ALIMENTACAO);
    }

    @ParameterizedTest
    @MethodSource("construtoresComUmComponenteNulo")
    void deve_lancar_quando_um_componente_e_nulo(ThrowingCallable construtor) {
        // Agir e Verificar
        assertThatThrownBy(construtor).isInstanceOf(NullPointerException.class);
    }

    @Test
    void deve_ser_igual_quando_todos_os_componentes_sao_iguais() {
        // Verificar
        assertThat(extrato()).isEqualTo(extrato()).hasSameHashCodeAs(extrato());
    }

    @Test
    void deve_nao_revelar_o_numero_quando_converter_em_texto() {
        // Agir
        var texto = extrato().toString();

        // Verificar
        assertThat(texto).contains("****2222").doesNotContain("5555000011112222");
    }
}
