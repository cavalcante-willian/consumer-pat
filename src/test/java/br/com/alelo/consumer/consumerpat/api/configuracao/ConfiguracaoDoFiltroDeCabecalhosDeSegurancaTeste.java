package br.com.alelo.consumer.consumerpat.api.configuracao;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfiguracaoDoFiltroDeCabecalhosDeSegurancaTeste {

    @Test
    void deve_registrar_na_posicao_dos_cabecalhos_de_seguranca_quando_ler_a_ordem() {
        // Agir
        var ordem = new ConfiguracaoDoFiltroDeCabecalhosDeSeguranca()
                .filtroDeCabecalhosDeSeguranca().getOrder();

        // Verificar
        assertThat(ordem).isEqualTo(OrdemDosFiltros.CABECALHOS_DE_SEGURANCA);
    }

    @Test
    void deve_aplicar_so_a_api_quando_ler_os_padroes_de_url() {
        // Agir
        var registro = new ConfiguracaoDoFiltroDeCabecalhosDeSeguranca()
                .filtroDeCabecalhosDeSeguranca();

        // Verificar: o Swagger e o console do H2 usam quadros e recursos que a política quebraria
        assertThat(registro.getUrlPatterns()).containsExactly("/api/*");
    }
}
