package br.com.alelo.consumer.consumerpat.api.comum.problema;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemaDeBordaEmMensagemTeste {

    @Test
    void deve_tirar_tipo_titulo_e_status_do_enum_quando_montar_o_corpo() {
        // Agir
        var problema = ProblemaDeBordaEmMensagem.montar(ProblemaDeBorda.RECURSO_EM_USO, "em uso",
                "/api/v1/compras", Map.of());

        // Verificar
        assertThat(problema.type()).isEqualTo("urn:consumer-pat:erro:CPT-CNF-SIS-002");
        assertThat(problema.title()).isEqualTo("Recurso em uso");
        assertThat(problema.status()).isEqualTo(409);
    }

    @Test
    void deve_levar_o_detalhe_o_caminho_e_os_campos_quando_montar_o_corpo() {
        // Preparar
        var erros = Map.of("valor", List.of("deve ser maior que zero"));

        // Agir
        var problema = ProblemaDeBordaEmMensagem.montar(ProblemaDeBorda.ENTRADA_INVALIDA, "corrija",
                "/api/v1/creditos", erros);

        // Verificar
        assertThat(problema.detail()).isEqualTo("corrija");
        assertThat(problema.instance()).isEqualTo("/api/v1/creditos");
        assertThat(problema.errors()).isEqualTo(erros);
    }
}
