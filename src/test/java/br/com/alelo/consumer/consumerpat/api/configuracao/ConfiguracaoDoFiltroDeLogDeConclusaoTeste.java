package br.com.alelo.consumer.consumerpat.api.configuracao;

import br.com.alelo.consumer.consumerpat.aplicacao.fakes.SaidaDeLogFake;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfiguracaoDoFiltroDeLogDeConclusaoTeste {

    @Test
    void deve_registrar_na_posicao_do_log_de_conclusao_quando_ler_a_ordem() {
        // Agir
        var ordem = new ConfiguracaoDoFiltroDeLogDeConclusao()
                .filtroDeLogDeConclusao(new SaidaDeLogFake()).getOrder();

        // Verificar
        assertThat(ordem).isEqualTo(OrdemDosFiltros.LOG_DE_CONCLUSAO);
    }
}
