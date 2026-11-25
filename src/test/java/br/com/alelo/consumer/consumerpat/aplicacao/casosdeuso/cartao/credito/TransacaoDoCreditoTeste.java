package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.credito;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoCredito;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.CartaoPortaFake;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.GerenciadorDeTransacaoFake;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Prova, num contexto Spring de verdade, que creditar um cartão abre uma transação de escrita, com
 * a operação num método {@code default} de um papel: o proxy da classe a enxerga e a confirma.
 */
@SpringJUnitConfig(TransacaoDoCreditoTeste.Configuracao.class)
class TransacaoDoCreditoTeste {

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
        CreditarCartaoCaso caso(CartaoPortaFake cartoes) {
            return new CreditarCartaoCaso(cartoes, cartoes);
        }
    }

    @Autowired
    private GerenciadorDeTransacaoFake gerenciador;

    @Autowired
    private CreditarCartaoCaso caso;

    private static DtoCredito credito(String numero, String valor) {
        return new DtoCredito(new NumeroDeCartao(numero), TipoCartao.ALIMENTACAO,
                new Dinheiro(new BigDecimal(valor)));
    }

    @Test
    void deve_abrir_uma_transacao_de_escrita_e_confirma_la_quando_o_credito_e_efetivado() {
        // Preparar
        var abertasAntes = gerenciador.abertas().size();
        var confirmadasAntes = gerenciador.confirmadas();

        // Agir
        var resultado = caso.creditar(credito(FabricaDeCartoes.NUMERO_PADRAO, "10.00"));

        // Verificar: a transação é de escrita, e não de leitura
        assertInstanceOf(Sucesso.class, resultado);
        assertThat(gerenciador.abertas()).hasSize(abertasAntes + 1);
        assertThat(gerenciador.abertas().get(abertasAntes).isReadOnly()).isFalse();
        assertThat(gerenciador.confirmadas()).isEqualTo(confirmadasAntes + 1);
    }

    @Test
    void deve_abrir_e_confirmar_a_transacao_mesmo_quando_o_credito_e_recusado() {
        // Preparar
        var abertasAntes = gerenciador.abertas().size();
        var confirmadasAntes = gerenciador.confirmadas();

        // Agir: um cartão que não existe é uma Falha devolvida, e não uma exceção
        var resultado = caso.creditar(credito("4111111111111111", "10.00"));

        // Verificar
        assertInstanceOf(Falha.class, resultado);
        assertThat(gerenciador.abertas()).hasSize(abertasAntes + 1);
        assertThat(gerenciador.confirmadas()).isEqualTo(confirmadasAntes + 1);
    }
}
