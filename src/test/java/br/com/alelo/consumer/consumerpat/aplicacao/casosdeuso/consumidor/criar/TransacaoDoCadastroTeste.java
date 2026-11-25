package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.criar;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoNovoConsumidor;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.GerenciadorDeTransacaoFake;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.SaidaDeLogFake;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.consumidor.ConsumidorPortaFake;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeConsumidores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova, num contexto Spring de verdade, que a transação do cadastro chega ao proxy.
 *
 * <p>
 * A operação mora num método {@code default} de um papel, e não na classe. A anotação ser achada
 * por reflexão não prova que o Spring a aplica ali, então este teste chama o bean e confere que o
 * gerenciador de transação foi pedido. O proxy é por classe, como o Spring Boot configura por
 * padrão.
 */
@SpringJUnitConfig(TransacaoDoCadastroTeste.Configuracao.class)
class TransacaoDoCadastroTeste {

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    static class Configuracao {

        @Bean
        GerenciadorDeTransacaoFake gerenciador() {
            return new GerenciadorDeTransacaoFake();
        }

        @Bean
        CriarConsumidorCaso caso() {
            return new CriarConsumidorCaso(new ConsumidorPortaFake(), new SaidaDeLogFake());
        }
    }

    @Autowired
    private GerenciadorDeTransacaoFake gerenciador;

    @Autowired
    private CriarConsumidorCaso caso;

    @Test
    void deve_abrir_uma_transacao_de_escrita_e_confirma_la_quando_criar_um_consumidor() {
        // Agir
        caso.criar(new DtoNovoConsumidor(FabricaDeConsumidores.dados(), Map.of()));

        // Verificar
        assertThat(gerenciador.abertas()).hasSize(1);
        assertThat(gerenciador.abertas().get(0).isReadOnly()).isFalse();
        assertThat(gerenciador.confirmadas()).isEqualTo(1);
    }
}
