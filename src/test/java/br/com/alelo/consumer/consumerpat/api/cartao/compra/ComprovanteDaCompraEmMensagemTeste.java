package br.com.alelo.consumer.consumerpat.api.cartao.compra;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoComprovanteCompra;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ComprovanteDaCompraEmMensagemTeste {

    private static final Instant INSTANTE = Instant.parse("2026-11-24T12:00:00Z");

    private static Dinheiro dinheiro(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    private static DtoComprovanteCompra comprovante() {
        return new DtoComprovanteCompra(new NumeroDeCartao("5555000011112222"),
                TipoCartao.ALIMENTACAO, dinheiro("33.33"), dinheiro("29.997"), dinheiro("70.003"),
                INSTANTE);
    }

    @Test
    void deve_mascarar_o_numero_quando_montar_a_mensagem() {
        // Agir
        var mensagem = ComprovanteDaCompraEmMensagem.montar(comprovante());

        // Verificar
        assertThat(mensagem.numeroCartao()).isEqualTo("****2222");
        assertThat(mensagem.toString()).doesNotContain("5555000011112222");
    }

    @Test
    void deve_dar_o_dinheiro_como_texto_com_a_escala_toda_quando_montar_a_mensagem() {
        // Agir
        var mensagem = ComprovanteDaCompraEmMensagem.montar(comprovante());

        // Verificar
        assertThat(mensagem.valorInformado()).isEqualTo("33.330000");
        assertThat(mensagem.valorCobrado()).isEqualTo("29.997000");
        assertThat(mensagem.saldoRestante()).isEqualTo("70.003000");
    }

    @Test
    void deve_dar_o_tipo_pelo_nome_e_o_instante_quando_montar_a_mensagem() {
        // Agir
        var mensagem = ComprovanteDaCompraEmMensagem.montar(comprovante());

        // Verificar
        assertThat(mensagem.tipo()).isEqualTo("ALIMENTACAO");
        assertThat(mensagem.instante()).isEqualTo(INSTANTE);
    }

    @Test
    void deve_nunca_usar_notacao_cientifica_quando_o_valor_e_grande() {
        // Preparar
        var grande = new DtoComprovanteCompra(new NumeroDeCartao("5555000011112222"),
                TipoCartao.FARMACIA, dinheiro("1000000"), dinheiro("1000000"), dinheiro("0.000001"),
                INSTANTE);

        // Agir
        var mensagem = ComprovanteDaCompraEmMensagem.montar(grande);

        // Verificar
        assertThat(mensagem.valorInformado()).isEqualTo("1000000.000000");
        assertThat(mensagem.saldoRestante()).isEqualTo("0.000001").doesNotContain("E");
    }
}
