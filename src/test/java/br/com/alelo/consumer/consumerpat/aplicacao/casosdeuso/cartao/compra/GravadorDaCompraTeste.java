package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.compra;

import br.com.alelo.consumer.consumerpat.aplicacao.fakes.CartaoPortaFake;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.ExtratoPortaFake;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.extrato.Extrato;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeCartoes;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class GravadorDaCompraTeste {

    private static final NumeroDeCartao NUMERO = new NumeroDeCartao(FabricaDeCartoes.NUMERO_PADRAO);

    private static Dinheiro dinheiro(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    private static Extrato extrato() {
        return new Extrato("Mercado", "Arroz", Instant.parse("2026-11-24T12:00:00Z"), NUMERO,
                TipoCartao.ALIMENTACAO, dinheiro("100.00"), dinheiro("90"));
    }

    @Test
    void deve_gravar_o_cartao_debitado_quando_gravar_a_compra() {
        // Preparar
        var cartoes = new CartaoPortaFake(
                FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).construir());
        var gravador = new GravadorDaCompra(cartoes, new ExtratoPortaFake());
        var debitado = FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).comSaldo("10.00")
                .construir();

        // Agir
        gravador.gravar(debitado, extrato());

        // Verificar
        assertThat(cartoes.gravados()).containsExactly(debitado);
        assertThat(cartoes.estado(NUMERO, TipoCartao.ALIMENTACAO)).contains(debitado);
    }

    @Test
    void deve_registrar_o_extrato_quando_gravar_a_compra() {
        // Preparar
        var extratos = new ExtratoPortaFake();
        var gravador = new GravadorDaCompra(
                new CartaoPortaFake(FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).construir()),
                extratos);

        // Agir
        gravador.gravar(FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).construir(), extrato());

        // Verificar
        assertThat(extratos.registrados()).containsExactly(extrato());
    }

    @Test
    void deve_devolver_o_cartao_como_ficou_gravado_quando_gravar_a_compra() {
        // Preparar
        var gravador = new GravadorDaCompra(
                new CartaoPortaFake(FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).construir()),
                new ExtratoPortaFake());
        var debitado = FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).comSaldo("10.00")
                .construir();

        // Agir
        var gravado = gravador.gravar(debitado, extrato());

        // Verificar
        assertThat(gravado).isEqualTo(debitado);
    }
}
