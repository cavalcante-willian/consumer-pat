package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.listar;

import br.com.alelo.consumer.consumerpat.aplicacao.fakes.GerenciadorDeTransacaoFake;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.consumidor.ConsumidorPortaFake;
import br.com.alelo.consumer.consumerpat.dominio.pagina.DirecaoDeOrdenacao;
import br.com.alelo.consumer.consumerpat.dominio.pagina.OrdenacaoDeConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.pagina.PedidoDePagina;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova, num contexto Spring de verdade, que a listagem abre uma transação só de leitura, com a
 * operação num método {@code default} de um papel.
 */
@SpringJUnitConfig(TransacaoDaListagemTeste.Configuracao.class)
class TransacaoDaListagemTeste {

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    static class Configuracao {

        @Bean
        GerenciadorDeTransacaoFake gerenciador() {
            return new GerenciadorDeTransacaoFake();
        }

        @Bean
        ListarConsumidoresCaso caso() {
            return new ListarConsumidoresCaso(new ConsumidorPortaFake());
        }
    }

    @Autowired
    private GerenciadorDeTransacaoFake gerenciador;

    @Autowired
    private ListarConsumidoresCaso caso;

    @Test
    void deve_abrir_uma_transacao_so_de_leitura_quando_listar_os_consumidores() {
        // Agir
        caso.listar(new PedidoDePagina(1, 20, OrdenacaoDeConsumidor.NOME, DirecaoDeOrdenacao.ASC));

        // Verificar
        assertThat(gerenciador.abertas()).hasSize(1);
        assertThat(gerenciador.abertas().get(0).isReadOnly()).isTrue();
    }
}
