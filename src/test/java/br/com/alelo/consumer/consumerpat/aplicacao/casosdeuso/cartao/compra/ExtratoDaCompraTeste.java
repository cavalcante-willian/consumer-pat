package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.compra;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoCompra;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeCartoes;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExtratoDaCompraTeste {

    private static final Instant INSTANTE = Instant.parse("2026-11-24T12:00:00Z");

    private static final NumeroDeCartao NUMERO = new NumeroDeCartao(FabricaDeCartoes.NUMERO_PADRAO);

    private static Dinheiro dinheiro(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    @Test
    void deve_guardar_os_dados_do_pedido_quando_montar_o_extrato() {
        // Preparar
        var compra = new DtoCompra(TipoCartao.ALIMENTACAO, "Mercado", NUMERO, "Arroz",
                dinheiro("100.00"));

        // Agir
        var extrato = ExtratoDaCompra.montar(compra, dinheiro("90"), INSTANTE);

        // Verificar
        assertThat(extrato.nomeDoEstabelecimento()).isEqualTo("Mercado");
        assertThat(extrato.descricaoDoProduto()).isEqualTo("Arroz");
        assertThat(extrato.numeroDoCartao()).isEqualTo(NUMERO);
        assertThat(extrato.tipo()).isEqualTo(TipoCartao.ALIMENTACAO);
    }

    @Test
    void deve_guardar_o_valor_informado_e_o_cobrado_distintos_quando_montar_o_extrato() {
        // Preparar
        var compra = new DtoCompra(TipoCartao.ALIMENTACAO, "Mercado", NUMERO, "Arroz",
                dinheiro("100.00"));

        // Agir
        var extrato = ExtratoDaCompra.montar(compra, dinheiro("90"), INSTANTE);

        // Verificar
        assertThat(extrato.valorInformado()).isEqualTo(dinheiro("100.00"));
        assertThat(extrato.valorCobrado()).isEqualTo(dinheiro("90"));
    }

    @Test
    void deve_usar_o_instante_recebido_quando_montar_o_extrato() {
        // Preparar
        var compra = new DtoCompra(TipoCartao.FARMACIA, "Drogaria", NUMERO, "Remédio",
                dinheiro("10.00"));

        // Agir
        var extrato = ExtratoDaCompra.montar(compra, dinheiro("10.00"), INSTANTE);

        // Verificar
        assertThat(extrato.dataDaCompra()).isEqualTo(INSTANTE);
    }

    @Test
    void deve_lancar_quando_um_texto_do_pedido_e_nulo() {
        // Preparar
        var compra = new DtoCompra(TipoCartao.FARMACIA, null, NUMERO, "Remédio", dinheiro("10.00"));

        // Agir e Verificar
        assertThatThrownBy(() -> ExtratoDaCompra.montar(compra, dinheiro("10.00"), INSTANTE))
                .isInstanceOf(NullPointerException.class);
    }
}
