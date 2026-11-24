package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.obter;

import br.com.alelo.consumer.consumerpat.aplicacao.fakes.GerenciadorDeTransacaoFake;
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
 * Prova, num contexto Spring de verdade, que a leitura abre uma transação só de leitura, com a
 * operação num método {@code default} de um papel.
 */
@SpringJUnitConfig(TransacaoDaLeituraTeste.Configuracao.class)
class TransacaoDaLeituraTeste {

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    static class Configuracao {

        @Bean
        GerenciadorDeTransacaoFake gerenciador() {
            return new GerenciadorDeTransacaoFake();
        }

        @Bean
        ObterConsumidorCaso caso() {
            return new ObterConsumidorCaso(
                    new ConsumidorPortaFake(FabricaDeConsumidores.consumidor(1, 0)));
        }
    }

    @Autowired
    private GerenciadorDeTransacaoFake gerenciador;

    @Autowired
    private ObterConsumidorCaso caso;

    @Test
    void deve_abrir_uma_transacao_so_de_leitura_quando_obter_um_consumidor() {
        // Agir
        caso.obter(new ConsumidorId(1));

        // Verificar
        assertThat(gerenciador.abertas()).hasSize(1);
        assertThat(gerenciador.abertas().get(0).isReadOnly()).isTrue();
    }
}
