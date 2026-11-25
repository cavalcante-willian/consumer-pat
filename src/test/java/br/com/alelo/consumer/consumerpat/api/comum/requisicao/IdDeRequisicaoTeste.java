package br.com.alelo.consumer.consumerpat.api.comum.requisicao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IdDeRequisicaoTeste {

    @ParameterizedTest
    @ValueSource(strings = { "0f1c9c1e-8a45-4d5e-9f77-1a2b3c4d5e6f",
            "0F1C9C1E-8A45-4D5E-9F77-1A2B3C4D5E6F", "00000000-0000-0000-0000-000000000000" })
    void deve_reaproveitar_o_id_quando_o_cliente_manda_um_uuid(String id) {
        // Agir e Verificar
        assertThat(IdDeRequisicao.aceitarOuGerar(id)).isEqualTo(id);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "abc", "1-1-1-1-1", "0f1c9c1e-8a45-4d5e-9f77-1a2b3c4d5e6",
            "0f1c9c1e-8a45-4d5e-9f77-1a2b3c4d5e6f0", "0f1c9c1e8a454d5e9f771a2b3c4d5e6f",
            "0f1c9c1e-8a45-4d5e-9f77-1a2b3c4d5e6g", "0f1c9c1e-8a45-4d5e-9f77-1a2b3c4d5e6f\n",
            "0f1c9c1e-8a45-4d5e-9f77-1a2b3c4d5e6f\r\nINFO forjada",
            " 0f1c9c1e-8a45-4d5e-9f77-1a2b3c4d5e6f", "<script>alert(1)</script>", "12345678901" })
    void deve_gerar_outro_quando_o_valor_do_cliente_nao_e_um_uuid(String id) {
        // Agir
        var gerado = IdDeRequisicao.aceitarOuGerar(id);

        // Verificar: texto livre do cliente num log é injeção de log
        assertThat(gerado).isNotEqualTo(id);
        assertThat(UUID.fromString(gerado)).isNotNull();
        assertThat(gerado).hasSize(36);
    }

    @Test
    void deve_gerar_um_id_diferente_a_cada_chamada_quando_nao_ha_cabecalho() {
        // Agir
        var primeiro = IdDeRequisicao.aceitarOuGerar(null);
        var segundo = IdDeRequisicao.aceitarOuGerar(null);

        // Verificar
        assertThat(primeiro).isNotEqualTo(segundo);
    }

    @Test
    void deve_usar_o_nome_do_cabecalho_e_da_chave_do_contrato() {
        // Verificar: o padrão do log lê %X{idRequisicao}
        assertThat(IdDeRequisicao.CABECALHO).isEqualTo("X-Request-Id");
        assertThat(IdDeRequisicao.CHAVE_NO_MDC).isEqualTo("idRequisicao");
    }
}
