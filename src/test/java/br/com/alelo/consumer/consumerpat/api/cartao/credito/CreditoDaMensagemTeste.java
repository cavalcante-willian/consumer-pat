package br.com.alelo.consumer.consumerpat.api.cartao.credito;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoCredito;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroNumeroDeCartaoInvalido;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroTipoEstabelecimentoInvalido;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CreditoDaMensagemTeste {

    private static MensagemCreditarCartao mensagem(String numero, int tipo, String valor) {
        return new MensagemCreditarCartao(numero, tipo, new BigDecimal(valor));
    }

    @SuppressWarnings("unchecked")
    private static DtoCredito pedidoDe(Resultado<DtoCredito, ErroDominio> resultado) {
        return ((Sucesso<DtoCredito, ErroDominio>) assertInstanceOf(Sucesso.class, resultado))
                .valor();
    }

    @ParameterizedTest
    @CsvSource({ "1, ALIMENTACAO", "2, FARMACIA", "3, COMBUSTIVEL" })
    void deve_converter_o_codigo_no_tipo_do_cartao_quando_a_mensagem_e_valida(int codigo,
            TipoCartao esperado) {
        // Agir
        var resultado = CreditoDaMensagem.converter(mensagem("5555000011112222", codigo, "10.00"));

        // Verificar
        assertThat(pedidoDe(resultado).tipo()).isEqualTo(esperado);
    }

    @Test
    void deve_levar_o_numero_e_o_valor_para_o_pedido_quando_a_mensagem_e_valida() {
        // Agir
        var pedido = pedidoDe(
                CreditoDaMensagem.converter(mensagem("5555 0000-1111 2222", 1, "50")));

        // Verificar
        assertThat(pedido.numeroCartao()).isEqualTo(new NumeroDeCartao("5555000011112222"));
        assertThat(pedido.valor()).isEqualTo(new Dinheiro(new BigDecimal("50")));
    }

    @Test
    void deve_falhar_com_tipo_invalido_quando_o_codigo_nao_existe() {
        // Agir
        var resultado = CreditoDaMensagem.converter(mensagem("5555000011112222", 99, "10.00"));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroTipoEstabelecimentoInvalido.class);
    }

    @Test
    void deve_falhar_com_numero_invalido_quando_o_numero_nao_tem_o_formato() {
        // Agir
        var resultado = CreditoDaMensagem.converter(mensagem("123", 1, "10.00"));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroNumeroDeCartaoInvalido.class);
    }
}
