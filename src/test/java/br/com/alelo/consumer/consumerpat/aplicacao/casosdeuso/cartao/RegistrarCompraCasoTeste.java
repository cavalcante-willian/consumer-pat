package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoCompra;
import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoComprovanteCompra;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.CartaoPortaFake;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.ExtratoPortaFake;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado.ErroCartaoNaoEncontrado;
import br.com.alelo.consumer.consumerpat.dominio.erros.regradenegocio.ErroSaldoInsuficiente;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroValorNaoPositivo;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeCartoes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RegistrarCompraCasoTeste {

    private static final Instant INSTANTE = Instant.parse("2026-11-24T12:00:00Z");

    private static final Clock RELOGIO = Clock.fixed(INSTANTE, ZoneOffset.UTC);

    private static final NumeroDeCartao NUMERO = new NumeroDeCartao(FabricaDeCartoes.NUMERO_PADRAO);

    private static Dinheiro dinheiro(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    private static DtoCompra compra(TipoCartao tipo, String valor) {
        return new DtoCompra(tipo, "Mercado", NUMERO, "Arroz", dinheiro(valor));
    }

    @SuppressWarnings("unchecked")
    private static Sucesso<DtoComprovanteCompra, ErroDominio> sucessoDe(
            Resultado<DtoComprovanteCompra, ErroDominio> resultado) {
        return (Sucesso<DtoComprovanteCompra, ErroDominio>) assertInstanceOf(Sucesso.class,
                resultado);
    }

    private final ExtratoPortaFake extratos = new ExtratoPortaFake();

    private RegistrarCompraCaso caso(CartaoPortaFake cartoes) {
        return new RegistrarCompraCaso(cartoes, new GravadorDaCompra(cartoes, extratos), RELOGIO);
    }

    @ParameterizedTest
    @CsvSource({ "ALIMENTACAO, 200.00, 100.00, 90, 110", "FARMACIA, 200.00, 100.00, 100, 100",
            "COMBUSTIVEL, 200.00, 100.00, 135, 65", "ALIMENTACAO, 100.00, 33.33, 29.997, 70.003" })
    void deve_debitar_o_valor_com_o_ajuste_do_tipo_quando_o_saldo_cobre(TipoCartao tipo,
            String saldo, String informado, String cobrado, String saldoFinal) {
        // Preparar
        var cartoes = new CartaoPortaFake(
                FabricaDeCartoes.doTipo(tipo).comSaldo(saldo).construir());

        // Agir
        var resultado = caso(cartoes).registrar(compra(tipo, informado));

        // Verificar
        var sucesso = sucessoDe(resultado);
        assertThat(sucesso.valor().valorInformado()).isEqualTo(dinheiro(informado));
        assertThat(sucesso.valor().valorCobrado()).isEqualTo(dinheiro(cobrado));
        assertThat(sucesso.valor().saldoRestante()).isEqualTo(dinheiro(saldoFinal));
        assertThat(cartoes.estado(NUMERO, tipo).orElseThrow().saldo())
                .isEqualTo(dinheiro(saldoFinal));
    }

    @Test
    void deve_registrar_o_extrato_com_os_dois_valores_e_o_instante_quando_a_compra_tem_sucesso() {
        // Preparar
        var cartoes = new CartaoPortaFake(
                FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).comSaldo("200.00").construir());

        // Agir
        caso(cartoes).registrar(compra(TipoCartao.ALIMENTACAO, "100.00"));

        // Verificar
        assertThat(extratos.registrados()).hasSize(1);
        var extrato = extratos.registrados().get(0);
        assertThat(extrato.nomeDoEstabelecimento()).isEqualTo("Mercado");
        assertThat(extrato.descricaoDoProduto()).isEqualTo("Arroz");
        assertThat(extrato.dataDaCompra()).isEqualTo(INSTANTE);
        assertThat(extrato.numeroDoCartao()).isEqualTo(NUMERO);
        assertThat(extrato.tipo()).isEqualTo(TipoCartao.ALIMENTACAO);
        assertThat(extrato.valorInformado()).isEqualTo(dinheiro("100.00"));
        assertThat(extrato.valorCobrado()).isEqualTo(dinheiro("90"));
    }

    @Test
    void deve_devolver_no_comprovante_o_instante_do_relogio_quando_a_compra_tem_sucesso() {
        // Preparar
        var cartoes = new CartaoPortaFake(FabricaDeCartoes.doTipo(TipoCartao.FARMACIA).construir());

        // Agir
        var resultado = caso(cartoes).registrar(compra(TipoCartao.FARMACIA, "10.00"));

        // Verificar
        var sucesso = sucessoDe(resultado);
        assertThat(sucesso.valor().instante()).isEqualTo(INSTANTE);
        assertThat(sucesso.valor().tipo()).isEqualTo(TipoCartao.FARMACIA);
    }

    @Test
    void deve_aceitar_a_compra_quando_o_desconto_faz_o_valor_cobrado_caber_no_saldo() {
        // Preparar
        var cartoes = new CartaoPortaFake(
                FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).comSaldo("95.00").construir());

        // Agir
        var resultado = caso(cartoes).registrar(compra(TipoCartao.ALIMENTACAO, "100.00"));

        // Verificar
        var sucesso = sucessoDe(resultado);
        assertThat(sucesso.valor().saldoRestante()).isEqualTo(dinheiro("5"));
    }

    @Test
    void deve_recusar_a_compra_quando_o_acrescimo_faz_o_valor_cobrado_passar_do_saldo() {
        // Preparar
        var cartoes = new CartaoPortaFake(
                FabricaDeCartoes.doTipo(TipoCartao.COMBUSTIVEL).comSaldo("120.00").construir());

        // Agir
        var resultado = caso(cartoes).registrar(compra(TipoCartao.COMBUSTIVEL, "100.00"));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        var erro = assertInstanceOf(ErroSaldoInsuficiente.class, falha.erro());
        assertThat(erro.saldo()).isEqualTo(dinheiro("120.00"));
        assertThat(erro.valor()).isEqualTo(dinheiro("135"));
    }

    @Test
    void deve_zerar_o_saldo_quando_o_valor_cobrado_e_exatamente_o_saldo() {
        // Preparar
        var cartoes = new CartaoPortaFake(
                FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).comSaldo("90.00").construir());

        // Agir
        var resultado = caso(cartoes).registrar(compra(TipoCartao.ALIMENTACAO, "100.00"));

        // Verificar
        var sucesso = sucessoDe(resultado);
        assertThat(sucesso.valor().saldoRestante()).isEqualTo(Dinheiro.ZERO);
    }

    @Test
    void deve_nao_gravar_nada_quando_o_saldo_e_insuficiente() {
        // Preparar
        var cartoes = new CartaoPortaFake(
                FabricaDeCartoes.doTipo(TipoCartao.FARMACIA).comSaldo("10.00").construir());

        // Agir
        caso(cartoes).registrar(compra(TipoCartao.FARMACIA, "50.00"));

        // Verificar
        assertThat(cartoes.gravados()).isEmpty();
        assertThat(extratos.registrados()).isEmpty();
        assertThat(cartoes.estado(NUMERO, TipoCartao.FARMACIA).orElseThrow().saldo())
                .isEqualTo(dinheiro("10.00"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", "0.000000", "-0.000001", "-5.00" })
    void deve_recusar_valor_nao_positivo_sem_gravar_nada_quando_o_valor_e_zero_ou_negativo(
            String valor) {
        // Preparar
        var cartoes = new CartaoPortaFake(
                FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).construir());

        // Agir
        var resultado = caso(cartoes).registrar(compra(TipoCartao.ALIMENTACAO, valor));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroValorNaoPositivo.class);
        assertThat(cartoes.gravados()).isEmpty();
        assertThat(extratos.registrados()).isEmpty();
    }

    @Test
    void deve_ser_cartao_nao_encontrado_quando_o_cartao_nao_existe() {
        // Preparar
        var cartoes = new CartaoPortaFake();

        // Agir
        var resultado = caso(cartoes).registrar(compra(TipoCartao.ALIMENTACAO, "10.00"));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro())
                .isEqualTo(new ErroCartaoNaoEncontrado(NUMERO, TipoCartao.ALIMENTACAO));
        assertThat(extratos.registrados()).isEmpty();
    }

    @Test
    void deve_ser_cartao_nao_encontrado_quando_o_numero_existe_com_outro_tipo() {
        // Preparar
        var cartoes = new CartaoPortaFake(FabricaDeCartoes.doTipo(TipoCartao.FARMACIA).construir());

        // Agir
        var resultado = caso(cartoes).registrar(compra(TipoCartao.ALIMENTACAO, "10.00"));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroCartaoNaoEncontrado.class);
        assertThat(cartoes.estado(NUMERO, TipoCartao.FARMACIA).orElseThrow().saldo())
                .isEqualTo(dinheiro("100.00"));
    }

    @Test
    void deve_nao_revelar_o_numero_quando_converter_o_comprovante_em_texto() {
        // Preparar
        var cartoes = new CartaoPortaFake(FabricaDeCartoes.doTipo(TipoCartao.FARMACIA).construir());

        // Agir
        var resultado = caso(cartoes).registrar(compra(TipoCartao.FARMACIA, "10.00"));

        // Verificar
        var sucesso = sucessoDe(resultado);
        assertThat(sucesso.valor().toString()).contains("****2222")
                .doesNotContain("5555000011112222");
    }

    @Test
    void deve_ser_transacional_quando_consultar_a_anotacao_do_metodo() throws Exception {
        // Agir
        var metodo = RegistrarCompraCaso.class.getMethod("registrar", DtoCompra.class);

        // Verificar
        assertThat(metodo.isAnnotationPresent(Transactional.class)).isTrue();
    }
}
