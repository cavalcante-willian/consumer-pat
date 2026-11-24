package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoComprovanteCredito;
import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoCredito;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.CartaoPortaFake;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado.ErroCartaoNaoEncontrado;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroValorNaoPositivo;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeCartoes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CreditarCartaoCasoTeste {

    private static final NumeroDeCartao NUMERO = new NumeroDeCartao(FabricaDeCartoes.NUMERO_PADRAO);

    private static Dinheiro dinheiro(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    private static DtoCredito credito(TipoCartao tipo, String valor) {
        return new DtoCredito(NUMERO, tipo, dinheiro(valor));
    }

    @SuppressWarnings("unchecked")
    private static Sucesso<DtoComprovanteCredito, ErroDominio> sucessoDe(
            Resultado<DtoComprovanteCredito, ErroDominio> resultado) {
        return (Sucesso<DtoComprovanteCredito, ErroDominio>) assertInstanceOf(Sucesso.class,
                resultado);
    }

    @ParameterizedTest
    @EnumSource(TipoCartao.class)
    void deve_somar_o_valor_informado_sem_ajuste_quando_creditar_em_qualquer_tipo(TipoCartao tipo) {
        // Preparar
        var cartoes = new CartaoPortaFake(
                FabricaDeCartoes.doTipo(tipo).comSaldo("50.00").construir());
        var caso = new CreditarCartaoCaso(cartoes);

        // Agir
        var resultado = caso.creditar(credito(tipo, "100.00"));

        // Verificar
        var sucesso = sucessoDe(resultado);
        assertThat(sucesso.valor().valorCreditado()).isEqualTo(dinheiro("100.00"));
        assertThat(sucesso.valor().saldoAtual()).isEqualTo(dinheiro("150.00"));
        assertThat(cartoes.estado(NUMERO, tipo).orElseThrow().saldo())
                .isEqualTo(dinheiro("150.00"));
    }

    @Test
    void deve_guardar_a_fracao_de_centavo_quando_creditar_valor_com_mais_de_duas_casas() {
        // Preparar
        var cartoes = new CartaoPortaFake(
                FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).comSaldo("0.00").construir());
        var caso = new CreditarCartaoCaso(cartoes);

        // Agir
        var resultado = caso.creditar(credito(TipoCartao.ALIMENTACAO, "29.997"));

        // Verificar
        var sucesso = sucessoDe(resultado);
        assertThat(sucesso.valor().saldoAtual()).isEqualTo(dinheiro("29.997"));
    }

    @Test
    void deve_gravar_o_cartao_uma_vez_quando_o_credito_tem_sucesso() {
        // Preparar
        var cartoes = new CartaoPortaFake(FabricaDeCartoes.doTipo(TipoCartao.FARMACIA).construir());
        var caso = new CreditarCartaoCaso(cartoes);

        // Agir
        caso.creditar(credito(TipoCartao.FARMACIA, "10.00"));

        // Verificar
        assertThat(cartoes.gravados()).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", "0.000000", "-0.000001", "-5.00" })
    void deve_recusar_valor_nao_positivo_sem_gravar_nada_quando_o_valor_e_zero_ou_negativo(
            String valor) {
        // Preparar
        var cartoes = new CartaoPortaFake(
                FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).construir());
        var caso = new CreditarCartaoCaso(cartoes);

        // Agir
        var resultado = caso.creditar(credito(TipoCartao.ALIMENTACAO, valor));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroValorNaoPositivo.class);
        assertThat(cartoes.gravados()).isEmpty();
        assertThat(cartoes.estado(NUMERO, TipoCartao.ALIMENTACAO).orElseThrow().saldo())
                .isEqualTo(dinheiro("100.00"));
    }

    @Test
    void deve_ser_cartao_nao_encontrado_quando_o_cartao_nao_existe() {
        // Preparar
        var caso = new CreditarCartaoCaso(new CartaoPortaFake());

        // Agir
        var resultado = caso.creditar(credito(TipoCartao.ALIMENTACAO, "10.00"));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro())
                .isEqualTo(new ErroCartaoNaoEncontrado(NUMERO, TipoCartao.ALIMENTACAO));
    }

    @Test
    void deve_ser_cartao_nao_encontrado_e_nao_creditar_quando_o_numero_existe_com_outro_tipo() {
        // Preparar
        var cartoes = new CartaoPortaFake(FabricaDeCartoes.doTipo(TipoCartao.FARMACIA).construir());
        var caso = new CreditarCartaoCaso(cartoes);

        // Agir
        var resultado = caso.creditar(credito(TipoCartao.ALIMENTACAO, "10.00"));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroCartaoNaoEncontrado.class);
        assertThat(cartoes.estado(NUMERO, TipoCartao.FARMACIA).orElseThrow().saldo())
                .isEqualTo(dinheiro("100.00"));
    }

    @Test
    void deve_nao_revelar_o_numero_quando_converter_o_comprovante_em_texto() {
        // Preparar
        var caso = new CreditarCartaoCaso(
                new CartaoPortaFake(FabricaDeCartoes.doTipo(TipoCartao.FARMACIA).construir()));

        // Agir
        var resultado = caso.creditar(credito(TipoCartao.FARMACIA, "10.00"));

        // Verificar
        var sucesso = sucessoDe(resultado);
        assertThat(sucesso.valor().toString()).contains("****2222")
                .doesNotContain("5555000011112222");
    }

    @Test
    void deve_ser_transacional_quando_consultar_a_anotacao_do_metodo() throws Exception {
        // Agir
        var metodo = CreditarCartaoCaso.class.getMethod("creditar", DtoCredito.class);

        // Verificar
        assertThat(metodo.isAnnotationPresent(Transactional.class)).isTrue();
    }
}
