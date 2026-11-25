package br.com.alelo.consumer.consumerpat.api.consumidor.atualizar;

import br.com.alelo.consumer.consumerpat.api.ConfiguracaoDeCasosDeUsoComFakes;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.consumidor.ConsumidorPortaFake;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeConsumidores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AtualizarConsumidorController.class)
@Import(ConfiguracaoDeCasosDeUsoComFakes.class)
class AtualizarConsumidorControllerTeste {

    private static final MediaType PROBLEMA = MediaType.valueOf("application/problem+json");

    private static final String DADOS = """
            {"nome":"Maria Atualizada","documento":"98765432100","dataNascimento":"1990-05-17",
            "celular":"11999998888","email":"maria@exemplo.com.br","rua":"Rua Nova","numero":"1",
            "cidade":"Recife","pais":"Brasil","cep":"50000000"}""";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ConsumidorPortaFake consumidores;

    private long id;

    private static String corpo(long versao) {
        return "{\"versao\":" + versao + ",\"dados\":" + DADOS + "}";
    }

    @BeforeEach
    void semear() {
        consumidores.memoria().consumidores().clear();
        consumidores.memoria().gravacoes().clear();
        var sucesso = assertInstanceOf(Sucesso.class,
                consumidores.criar(FabricaDeConsumidores.novo()));
        id = ((Consumidor) sucesso.valor()).id().valor();
        consumidores.memoria().gravacoes().clear();
    }

    @Test
    void deve_responder_200_com_os_dados_novos_e_a_versao_avancada_quando_a_versao_e_a_atual()
            throws Exception {
        // Agir e Verificar
        mvc.perform(put("/api/v1/consumidores/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(corpo(0))).andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Atualizada"))
                .andExpect(jsonPath("$.documento").value("****2100"))
                .andExpect(jsonPath("$.versao").value(1));
    }

    @Test
    void deve_manter_os_cartoes_e_o_saldo_quando_atualizar_o_cadastro() throws Exception {
        // Preparar
        var antes = consumidores
                .estado(new br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId(id))
                .orElseThrow().cartoes();

        // Agir
        mvc.perform(put("/api/v1/consumidores/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(corpo(0))).andExpect(status().isOk())
                .andExpect(jsonPath("$.cartoes.length()").value(antes.cartoes().size()));

        // Verificar
        assertThat(consumidores.gravados().get(0).cartoes()).isEqualTo(antes);
    }

    @Test
    void deve_responder_409_e_nao_gravar_quando_a_versao_esta_velha() throws Exception {
        // Agir e Verificar
        mvc.perform(put("/api/v1/consumidores/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(corpo(7))).andExpect(status().isConflict())
                .andExpect(content().contentType(PROBLEMA))
                .andExpect(jsonPath("$.type").value("urn:consumer-pat:erro:CPT-CNF-CON-002"));
        assertThat(consumidores.gravados()).isEmpty();
    }

    @Test
    void deve_responder_404_quando_o_consumidor_nao_existe() throws Exception {
        // Agir e Verificar
        mvc.perform(put("/api/v1/consumidores/9999").contentType(MediaType.APPLICATION_JSON)
                .content(corpo(0))).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("urn:consumer-pat:erro:CPT-NEN-CON-001"));
    }

    @Test
    void deve_responder_400_quando_o_id_da_rota_nao_e_positivo() throws Exception {
        // Agir e Verificar
        mvc.perform(put("/api/v1/consumidores/0").contentType(MediaType.APPLICATION_JSON)
                .content(corpo(0))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.id").isArray());
    }

    @Test
    void deve_responder_400_apontando_o_id_quando_ele_nao_e_numero() throws Exception {
        // Agir e Verificar
        mvc.perform(put("/api/v1/consumidores/abc").contentType(MediaType.APPLICATION_JSON)
                .content(corpo(0))).andExpect(status().isBadRequest())
                .andExpect(content().contentType(PROBLEMA))
                .andExpect(jsonPath("$.errors.id[0]").value("não tem o formato esperado"));
    }

    @Test
    void deve_reunir_os_erros_de_conversao_quando_o_documento_e_o_cep_sao_invalidos()
            throws Exception {
        // Agir e Verificar
        mvc.perform(put("/api/v1/consumidores/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(corpo(0).replace("98765432100", "123").replace("50000000", "5")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.documento").isArray())
                .andExpect(jsonPath("$.errors.cep").isArray());
    }

    @Test
    void deve_recusar_um_saldo_no_corpo_quando_a_atualizacao_tenta_mexer_em_dinheiro()
            throws Exception {
        // Agir e Verificar
        mvc.perform(put("/api/v1/consumidores/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(corpo(0).replace("\"dados\"", "\"saldo\":999,\"dados\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.saldo[0]").value("não é aceito nesta operação"));
        assertThat(consumidores.gravados()).isEmpty();
    }

    @Test
    void deve_responder_400_quando_a_versao_e_negativa() throws Exception {
        // Agir e Verificar
        mvc.perform(put("/api/v1/consumidores/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(corpo(-1))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.versao").isArray());
    }
}
