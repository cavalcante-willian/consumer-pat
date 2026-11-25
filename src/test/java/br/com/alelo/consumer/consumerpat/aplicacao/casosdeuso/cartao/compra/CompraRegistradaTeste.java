package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.compra;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoComprovanteCompra;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.SaidaDeLogFake;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.log.NivelDeLog;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CompraRegistradaTeste {

    private final SaidaDeLogFake saida = new SaidaDeLogFake();

    private static Dinheiro dinheiro(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    private static DtoComprovanteCompra comprovante() {
        return new DtoComprovanteCompra(new NumeroDeCartao("5555000011112222"),
                TipoCartao.COMBUSTIVEL, dinheiro("100.00"), dinheiro("135"), dinheiro("65"),
                Instant.parse("2026-11-24T12:00:00Z"));
    }

    @Test
    void deve_registrar_em_informacao_o_tipo_e_o_valor_cobrado_e_nao_o_informado() {
        // Agir
        CompraRegistrada.registrar(saida, comprovante());

        // Verificar: o combustível cobra 35% a mais, e é isso que saiu do saldo
        assertThat(saida.eventos()).singleElement().satisfies(evento -> {
            assertThat(evento.nivel()).isEqualTo(NivelDeLog.INFORMACAO);
            assertThat(evento.evento()).isEqualTo("compra.registrada");
            assertThat(evento.valorDe("tipoCartao")).contains(TipoCartao.COMBUSTIVEL);
            assertThat(evento.valorDe("valorCobrado")).contains("135.000000");
        });
    }

    @Test
    void deve_registrar_o_cartao_so_mascarado() {
        // Agir
        CompraRegistrada.registrar(saida, comprovante());

        // Verificar: o número completo nunca aparece ao lado de um valor
        assertThat(saida.eventos().get(0).texto()).contains("****2222")
                .doesNotContain("5555000011112222");
    }

    @Test
    void deve_nao_levar_o_saldo_restante_ao_log() {
        // Agir
        CompraRegistrada.registrar(saida, comprovante());

        // Verificar: o saldo é dado do cliente, e o valor cobrado basta para o fato
        assertThat(saida.eventos().get(0).campos()).extracting(campo -> campo.nome())
                .containsExactly("cartao", "tipoCartao", "valorCobrado");
    }
}
