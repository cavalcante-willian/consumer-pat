package br.com.alelo.consumer.consumerpat.api.cartao.credito;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoComprovanteCredito;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ComprovanteDoCreditoEmMensagemTeste {

    private static Dinheiro dinheiro(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    private static DtoComprovanteCredito comprovante() {
        return new DtoComprovanteCredito(new NumeroDeCartao("5555000011112222"),
                TipoCartao.COMBUSTIVEL, dinheiro("25"), dinheiro("125.5"));
    }

    @Test
    void deve_mascarar_o_numero_quando_montar_a_mensagem() {
        // Agir
        var mensagem = ComprovanteDoCreditoEmMensagem.montar(comprovante());

        // Verificar
        assertThat(mensagem.numeroCartao()).isEqualTo("****2222");
        assertThat(mensagem.toString()).doesNotContain("5555000011112222");
    }

    @Test
    void deve_dar_o_dinheiro_como_texto_com_a_escala_toda_quando_montar_a_mensagem() {
        // Agir
        var mensagem = ComprovanteDoCreditoEmMensagem.montar(comprovante());

        // Verificar
        assertThat(mensagem.valorCreditado()).isEqualTo("25.000000");
        assertThat(mensagem.saldoAtual()).isEqualTo("125.500000");
    }

    @Test
    void deve_dar_o_tipo_pelo_nome_quando_montar_a_mensagem() {
        // Agir
        var mensagem = ComprovanteDoCreditoEmMensagem.montar(comprovante());

        // Verificar
        assertThat(mensagem.tipo()).isEqualTo("COMBUSTIVEL");
    }
}
