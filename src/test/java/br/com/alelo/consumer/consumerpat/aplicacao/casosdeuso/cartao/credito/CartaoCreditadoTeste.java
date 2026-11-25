package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.credito;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoComprovanteCredito;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.SaidaDeLogFake;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.log.NivelDeLog;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CartaoCreditadoTeste {

    private final SaidaDeLogFake saida = new SaidaDeLogFake();

    private static Dinheiro dinheiro(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    private static DtoComprovanteCredito comprovante() {
        return new DtoComprovanteCredito(new NumeroDeCartao("5555000011112222"),
                TipoCartao.ALIMENTACAO, dinheiro("100.00"), dinheiro("350.00"));
    }

    @Test
    void deve_registrar_em_informacao_o_tipo_e_o_valor_creditado() {
        // Agir
        CartaoCreditado.registrar(saida, comprovante());

        // Verificar
        assertThat(saida.eventos()).singleElement().satisfies(evento -> {
            assertThat(evento.nivel()).isEqualTo(NivelDeLog.INFORMACAO);
            assertThat(evento.evento()).isEqualTo("cartao.creditado");
            assertThat(evento.valorDe("tipoCartao")).contains(TipoCartao.ALIMENTACAO);
            assertThat(evento.valorDe("valor")).contains("100.000000");
        });
    }

    @Test
    void deve_registrar_o_cartao_so_mascarado_e_nao_o_saldo_que_ficou() {
        // Agir
        CartaoCreditado.registrar(saida, comprovante());

        // Verificar: o log não é a trilha financeira, o extrato é
        assertThat(saida.eventos().get(0).texto()).contains("****2222")
                .doesNotContain("5555000011112222").doesNotContain("350");
    }
}
