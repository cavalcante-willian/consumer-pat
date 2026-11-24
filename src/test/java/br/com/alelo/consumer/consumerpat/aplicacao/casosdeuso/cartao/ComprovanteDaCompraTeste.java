package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao;

import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.extrato.Extrato;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeCartoes;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ComprovanteDaCompraTeste {

    private static final Instant INSTANTE = Instant.parse("2026-11-24T12:00:00Z");

    private static final NumeroDeCartao NUMERO = new NumeroDeCartao(FabricaDeCartoes.NUMERO_PADRAO);

    private static Dinheiro dinheiro(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    private static Extrato extrato() {
        return new Extrato("Mercado", "Arroz", INSTANTE, NUMERO, TipoCartao.ALIMENTACAO,
                dinheiro("100.00"), dinheiro("90"));
    }

    @Test
    void deve_tirar_os_valores_e_o_instante_do_extrato_quando_montar_o_comprovante() {
        // Preparar
        var debitado = FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).comSaldo("110.00")
                .construir();

        // Agir
        var comprovante = ComprovanteDaCompra.montar(extrato(), debitado);

        // Verificar
        assertThat(comprovante.valorInformado()).isEqualTo(dinheiro("100.00"));
        assertThat(comprovante.valorCobrado()).isEqualTo(dinheiro("90"));
        assertThat(comprovante.instante()).isEqualTo(INSTANTE);
        assertThat(comprovante.numeroCartao()).isEqualTo(NUMERO);
        assertThat(comprovante.tipo()).isEqualTo(TipoCartao.ALIMENTACAO);
    }

    @Test
    void deve_tirar_o_saldo_restante_do_cartao_quando_montar_o_comprovante() {
        // Preparar
        var debitado = FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).comSaldo("110.00")
                .construir();

        // Agir
        var comprovante = ComprovanteDaCompra.montar(extrato(), debitado);

        // Verificar
        assertThat(comprovante.saldoRestante()).isEqualTo(dinheiro("110.00"));
    }

    @Test
    void deve_nao_revelar_o_numero_quando_converter_o_comprovante_em_texto() {
        // Agir
        var comprovante = ComprovanteDaCompra.montar(extrato(),
                FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).construir());

        // Verificar
        assertThat(comprovante.toString()).contains("****2222").doesNotContain("5555000011112222");
    }
}
