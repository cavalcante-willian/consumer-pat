package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.compra;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoCompra;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.SaidaDeLogFake;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.erros.regradenegocio.ErroSaldoInsuficiente;
import br.com.alelo.consumer.consumerpat.dominio.log.NivelDeLog;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CompraRecusadaTeste {

    private final SaidaDeLogFake saida = new SaidaDeLogFake();

    private static Dinheiro dinheiro(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    private static final DtoCompra COMPRA = new DtoCompra(TipoCartao.FARMACIA, "Drogaria Central",
            new NumeroDeCartao("5555000011112222"), "Remédio", dinheiro("100.00"));

    private static final ErroSaldoInsuficiente ERRO = new ErroSaldoInsuficiente(dinheiro("10.00"),
            dinheiro("100.00"));

    @Test
    void deve_registrar_em_informacao_o_tipo_e_o_codigo_do_erro_como_motivo() {
        // Agir
        CompraRecusada.registrar(saida, COMPRA, ERRO);

        // Verificar: recusa de negócio é resultado esperado, e não falha do sistema
        assertThat(saida.eventos()).singleElement().satisfies(evento -> {
            assertThat(evento.nivel()).isEqualTo(NivelDeLog.INFORMACAO);
            assertThat(evento.evento()).isEqualTo("compra.recusada");
            assertThat(evento.valorDe("tipoCartao")).contains(TipoCartao.FARMACIA);
            assertThat(evento.valorDe("motivo")).contains(ErroSaldoInsuficiente.CODIGO);
        });
    }

    @Test
    void deve_nao_levar_a_mensagem_o_saldo_nem_o_texto_do_cliente_ao_log() {
        // Agir
        CompraRecusada.registrar(saida, COMPRA, ERRO);

        // Verificar: o motivo é o código, que liga a linha ao type da resposta
        assertThat(saida.eventos().get(0).texto()).contains("****2222")
                .doesNotContain("5555000011112222").doesNotContain("Drogaria")
                .doesNotContain("Remédio").doesNotContain(ERRO.mensagem());
    }
}
