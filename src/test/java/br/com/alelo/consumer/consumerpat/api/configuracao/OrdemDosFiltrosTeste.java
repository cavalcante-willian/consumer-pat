package br.com.alelo.consumer.consumerpat.api.configuracao;

import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova a ordem do pipeline: menor número é mais externo, e a ordem é parte do contrato.
 */
class OrdemDosFiltrosTeste {

    @Test
    void deve_ter_os_cabecalhos_de_seguranca_como_o_filtro_mais_externo() {
        // Verificar
        assertThat(OrdemDosFiltros.CABECALHOS_DE_SEGURANCA).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
    }

    @Test
    void deve_ordenar_do_mais_externo_ao_mais_interno_quando_listar_as_etapas() {
        // Agir
        var etapas = List.of(OrdemDosFiltros.CABECALHOS_DE_SEGURANCA, OrdemDosFiltros.CORRELACAO,
                OrdemDosFiltros.LOG_DE_CONCLUSAO, OrdemDosFiltros.REJEICAO_DE_TRACE);

        // Verificar: segurança, correlação, log de conclusão e, por último, a recusa do TRACE, que
        // assim sai no log com o id da requisição
        assertThat(etapas).isSorted().doesNotHaveDuplicates();
    }
}
