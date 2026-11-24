package br.com.alelo.consumer.consumerpat.api.cartao.compra;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoCompra;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroNumeroDeCartaoInvalido;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroTipoEstabelecimentoInvalido;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CompraDaMensagemTeste {

    private static MensagemRegistrarCompra mensagem(int tipo, String numero, String valor) {
        return new MensagemRegistrarCompra(tipo, "Mercado", numero, "Arroz", new BigDecimal(valor));
    }

    @SuppressWarnings("unchecked")
    private static DtoCompra pedidoDe(Resultado<DtoCompra, ErroDominio> resultado) {
        return ((Sucesso<DtoCompra, ErroDominio>) assertInstanceOf(Sucesso.class, resultado))
                .valor();
    }

    @ParameterizedTest
    @CsvSource({ "1, ALIMENTACAO", "2, FARMACIA", "3, COMBUSTIVEL" })
    void deve_converter_o_codigo_no_tipo_do_cartao_quando_a_mensagem_e_valida(int codigo,
            TipoCartao esperado) {
        // Agir
        var resultado = CompraDaMensagem.converter(mensagem(codigo, "5555000011112222", "10.00"));

        // Verificar
        assertThat(pedidoDe(resultado).tipo()).isEqualTo(esperado);
    }

    @Test
    void deve_levar_o_numero_o_valor_e_os_textos_para_o_pedido_quando_a_mensagem_e_valida() {
        // Agir
        var pedido = pedidoDe(CompraDaMensagem.converter(mensagem(1, "5555000011112222", "33.33")));

        // Verificar
        assertThat(pedido.numeroCartao()).isEqualTo(new NumeroDeCartao("5555000011112222"));
        assertThat(pedido.valor()).isEqualTo(new Dinheiro(new BigDecimal("33.33")));
        assertThat(pedido.nomeEstabelecimento()).isEqualTo("Mercado");
        assertThat(pedido.descricaoProduto()).isEqualTo("Arroz");
    }

    @Test
    void deve_normalizar_o_numero_quando_a_mensagem_traz_espacos_e_hifens() {
        // Agir
        var pedido = pedidoDe(
                CompraDaMensagem.converter(mensagem(2, "5555 0000-1111 2222", "10.00")));

        // Verificar
        assertThat(pedido.numeroCartao().digitos()).isEqualTo("5555000011112222");
    }

    @Test
    void deve_falhar_com_tipo_invalido_quando_o_codigo_de_estabelecimento_nao_existe() {
        // Agir
        var resultado = CompraDaMensagem.converter(mensagem(99, "5555000011112222", "10.00"));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroTipoEstabelecimentoInvalido.class);
    }

    @Test
    void deve_falhar_com_numero_invalido_quando_o_numero_nao_tem_o_formato() {
        // Agir
        var resultado = CompraDaMensagem.converter(mensagem(1, "123", "10.00"));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroNumeroDeCartaoInvalido.class);
    }

    @Test
    void deve_recusar_o_tipo_antes_do_numero_quando_os_dois_sao_invalidos() {
        // Agir
        var resultado = CompraDaMensagem.converter(mensagem(99, "123", "10.00"));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroTipoEstabelecimentoInvalido.class);
    }
}
