package br.com.alelo.consumer.consumerpat.api.configuracao;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfiguracaoDoFiltroDeCorrelacaoTeste {

    @Test
    void deve_registrar_na_posicao_da_correlacao_quando_ler_a_ordem() {
        // Agir
        var ordem = new ConfiguracaoDoFiltroDeCorrelacao().filtroDeCorrelacao().getOrder();

        // Verificar
        assertThat(ordem).isEqualTo(OrdemDosFiltros.CORRELACAO);
    }
}
