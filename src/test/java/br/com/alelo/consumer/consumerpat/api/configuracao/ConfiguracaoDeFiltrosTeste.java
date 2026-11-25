package br.com.alelo.consumer.consumerpat.api.configuracao;

import br.com.alelo.consumer.consumerpat.aplicacao.fakes.SaidaDeLogFake;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova a ordem do pipeline pelo registro: menor número é mais externo, e a ordem é parte do
 * contrato.
 */
class ConfiguracaoDeFiltrosTeste {

    private final ConfiguracaoDeFiltros configuracao = new ConfiguracaoDeFiltros();

    @Test
    void deve_registrar_os_cabecalhos_de_seguranca_como_o_filtro_mais_externo() {
        // Agir
        var seguranca = configuracao.filtroDeCabecalhosDeSeguranca().getOrder();
        var correlacao = configuracao.filtroDeCorrelacao().getOrder();
        var conclusao = configuracao.filtroDeLogDeConclusao(new SaidaDeLogFake()).getOrder();

        // Verificar: segurança, depois correlação, e só então o log de conclusão
        assertThat(seguranca).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
        assertThat(seguranca).isLessThan(correlacao);
        assertThat(correlacao).isLessThan(conclusao);
    }

    @Test
    void deve_aplicar_os_cabecalhos_de_seguranca_so_a_api() {
        // Agir
        var registro = configuracao.filtroDeCabecalhosDeSeguranca();

        // Verificar: o Swagger e o console do H2 usam quadros e recursos que a política quebraria
        assertThat(registro.getUrlPatterns()).containsExactly("/api/*");
    }
}
