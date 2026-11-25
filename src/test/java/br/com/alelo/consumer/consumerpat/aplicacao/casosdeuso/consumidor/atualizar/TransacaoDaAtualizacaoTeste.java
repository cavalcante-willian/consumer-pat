package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.atualizar;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoAtualizacaoDeConsumidor;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.GerenciadorDeTransacaoFake;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.SaidaDeLogFake;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.consumidor.ConsumidorPortaFake;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeConsumidores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova, num contexto Spring de verdade, que a transação da atualização chega ao proxy, com a
 * operação num método {@code default} de um papel.
 */
@SpringJUnitConfig(TransacaoDaAtualizacaoTeste.Configuracao.class)
class TransacaoDaAtualizacaoTeste {

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    static class Configuracao {

        @Bean
        GerenciadorDeTransacaoFake gerenciador() {
            return new GerenciadorDeTransacaoFake();
        }

        @Bean
        AtualizarConsumidorCaso caso() {
            var porta = new ConsumidorPortaFake(FabricaDeConsumidores.consumidor(1, 0));
            return new AtualizarConsumidorCaso(porta, porta, new SaidaDeLogFake());
        }
    }

    @Autowired
    private GerenciadorDeTransacaoFake gerenciador;

    @Autowired
    private AtualizarConsumidorCaso caso;

    @Test
    void deve_abrir_uma_transacao_de_escrita_e_confirma_la_quando_atualizar_o_cadastro() {
        // Agir
        caso.atualizar(new DtoAtualizacaoDeConsumidor(new ConsumidorId(1), 0,
                FabricaDeConsumidores.dadosComDocumento("11122233344")));

        // Verificar
        assertThat(gerenciador.abertas()).hasSize(1);
        assertThat(gerenciador.abertas().get(0).isReadOnly()).isFalse();
        assertThat(gerenciador.confirmadas()).isEqualTo(1);
    }
}
