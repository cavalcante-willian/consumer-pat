package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.compra;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoCompra;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.CartaoPortaFake;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.ExtratoPortaFake;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.GerenciadorDeTransacaoFake;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.SaidaDeLogFake;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeCartoes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Prova, num contexto Spring de verdade, que registrar uma compra abre uma transação de escrita,
 * com a operação num método {@code default} de um papel: o proxy da classe a enxerga e a confirma.
 */
@SpringJUnitConfig(TransacaoDaCompraTeste.Configuracao.class)
class TransacaoDaCompraTeste {

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    static class Configuracao {

        @Bean
        GerenciadorDeTransacaoFake gerenciador() {
            return new GerenciadorDeTransacaoFake();
        }

        @Bean
        CartaoPortaFake cartoes() {
            return new CartaoPortaFake(FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).construir());
        }

        @Bean
        RegistrarCompraCaso caso(CartaoPortaFake cartoes) {
            return new RegistrarCompraCaso(cartoes,
                    new GravadorDaCompra(cartoes, new ExtratoPortaFake()),
                    Clock.fixed(Instant.parse("2026-11-24T12:00:00Z"), ZoneOffset.UTC),
                    new SaidaDeLogFake());
        }
    }

    @Autowired
    private GerenciadorDeTransacaoFake gerenciador;

    @Autowired
    private RegistrarCompraCaso caso;

    private static DtoCompra compra(String numero, String valor) {
        return new DtoCompra(TipoCartao.ALIMENTACAO, "Mercado", new NumeroDeCartao(numero), "Arroz",
                new Dinheiro(new BigDecimal(valor)));
    }

    @Test
    void deve_abrir_uma_transacao_de_escrita_e_confirma_la_quando_a_compra_e_efetivada() {
        // Preparar
        var abertasAntes = gerenciador.abertas().size();
        var confirmadasAntes = gerenciador.confirmadas();

        // Agir
        var resultado = caso.registrar(compra(FabricaDeCartoes.NUMERO_PADRAO, "10.00"));

        // Verificar: a transação é de escrita, e não de leitura
        assertInstanceOf(Sucesso.class, resultado);
        assertThat(gerenciador.abertas()).hasSize(abertasAntes + 1);
        assertThat(gerenciador.abertas().get(abertasAntes).isReadOnly()).isFalse();
        assertThat(gerenciador.confirmadas()).isEqualTo(confirmadasAntes + 1);
    }

    @Test
    void deve_abrir_e_confirmar_a_transacao_mesmo_quando_a_compra_e_recusada() {
        // Preparar
        var abertasAntes = gerenciador.abertas().size();
        var confirmadasAntes = gerenciador.confirmadas();

        // Agir: um cartão que não existe é uma Falha devolvida, e não uma exceção
        var resultado = caso.registrar(compra("4111111111111111", "10.00"));

        // Verificar: uma Falha não desfaz a transação, e não precisa, porque nada foi gravado
        assertInstanceOf(Falha.class, resultado);
        assertThat(gerenciador.abertas()).hasSize(abertasAntes + 1);
        assertThat(gerenciador.confirmadas()).isEqualTo(confirmadasAntes + 1);
    }
}
