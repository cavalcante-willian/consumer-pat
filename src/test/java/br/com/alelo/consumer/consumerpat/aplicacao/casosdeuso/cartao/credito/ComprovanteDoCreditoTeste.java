package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.credito;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoCredito;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeCartoes;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ComprovanteDoCreditoTeste {

    private static final NumeroDeCartao NUMERO = new NumeroDeCartao(FabricaDeCartoes.NUMERO_PADRAO);

    private static Dinheiro dinheiro(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    @Test
    void deve_tirar_o_que_foi_pedido_do_pedido_quando_montar_o_comprovante() {
        // Preparar
        var credito = new DtoCredito(NUMERO, TipoCartao.COMBUSTIVEL, dinheiro("100.00"));
        var creditado = FabricaDeCartoes.doTipo(TipoCartao.COMBUSTIVEL).comSaldo("150.00")
                .construir();

        // Agir
        var comprovante = ComprovanteDoCredito.montar(credito, creditado);

        // Verificar
        assertThat(comprovante.numeroCartao()).isEqualTo(NUMERO);
        assertThat(comprovante.tipo()).isEqualTo(TipoCartao.COMBUSTIVEL);
        assertThat(comprovante.valorCreditado()).isEqualTo(dinheiro("100.00"));
    }

    @Test
    void deve_tirar_o_saldo_atual_do_cartao_quando_montar_o_comprovante() {
        // Preparar
        var credito = new DtoCredito(NUMERO, TipoCartao.COMBUSTIVEL, dinheiro("100.00"));
        var creditado = FabricaDeCartoes.doTipo(TipoCartao.COMBUSTIVEL).comSaldo("150.00")
                .construir();

        // Agir
        var comprovante = ComprovanteDoCredito.montar(credito, creditado);

        // Verificar
        assertThat(comprovante.saldoAtual()).isEqualTo(dinheiro("150.00"));
    }

    @Test
    void deve_nao_revelar_o_numero_quando_converter_o_comprovante_em_texto() {
        // Preparar
        var credito = new DtoCredito(NUMERO, TipoCartao.FARMACIA, dinheiro("1.00"));

        // Agir
        var comprovante = ComprovanteDoCredito.montar(credito,
                FabricaDeCartoes.doTipo(TipoCartao.FARMACIA).construir());

        // Verificar
        assertThat(comprovante.toString()).contains("****2222").doesNotContain("5555000011112222");
    }
}
