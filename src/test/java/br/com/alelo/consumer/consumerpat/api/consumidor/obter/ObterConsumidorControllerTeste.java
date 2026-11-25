package br.com.alelo.consumer.consumerpat.api.consumidor.obter;

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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ObterConsumidorController.class)
@Import(ConfiguracaoDeCasosDeUsoComFakes.class)
class ObterConsumidorControllerTeste {

    private static final MediaType PROBLEMA = MediaType.valueOf("application/problem+json");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ConsumidorPortaFake consumidores;

    private long id;

    @BeforeEach
    void semear() {
        consumidores.memoria().consumidores().clear();
        var sucesso = assertInstanceOf(Sucesso.class,
                consumidores.criar(FabricaDeConsumidores.novo()));
        id = ((Consumidor) sucesso.valor()).id().valor();
    }

    @Test
    void deve_responder_200_com_o_consumidor_a_versao_e_os_cartoes_quando_o_id_existe()
            throws Exception {
        // Agir e Verificar
        mvc.perform(get("/api/v1/consumidores/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id)).andExpect(jsonPath("$.versao").value(0))
                .andExpect(jsonPath("$.nome").value("Maria Souza"))
                .andExpect(jsonPath("$.email").value("maria@exemplo.com.br"))
                .andExpect(jsonPath("$.cartoes[0].tipo").value("ALIMENTACAO"));
    }

    @Test
    void deve_mascarar_o_documento_o_celular_e_o_numero_quando_devolver_o_consumidor()
            throws Exception {
        // Agir e Verificar
        mvc.perform(get("/api/v1/consumidores/" + id))
                .andExpect(jsonPath("$.documento").value("****8901"))
                .andExpect(jsonPath("$.celular").value("****8888"))
                .andExpect(jsonPath("$.cartoes[0].numero").value("****2222"))
                .andExpect(content().string(not(containsString("12345678901"))))
                .andExpect(content().string(not(containsString("5555000011112222"))));
    }

    @Test
    void deve_omitir_os_telefones_que_nao_existem_quando_devolver_o_consumidor() throws Exception {
        // Agir e Verificar
        mvc.perform(get("/api/v1/consumidores/" + id))
                .andExpect(jsonPath("$.telefoneResidencial").doesNotExist())
                .andExpect(jsonPath("$.telefone").doesNotExist());
    }

    @Test
    void deve_responder_404_quando_o_id_nao_existe() throws Exception {
        // Agir e Verificar
        mvc.perform(get("/api/v1/consumidores/9999")).andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEMA))
                .andExpect(jsonPath("$.type").value("urn:consumer-pat:erro:CPT-NEN-CON-001"));
    }

    @Test
    void deve_responder_400_quando_o_id_nao_e_positivo() throws Exception {
        // Agir e Verificar
        mvc.perform(get("/api/v1/consumidores/0")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.id").isArray());
    }

    @Test
    void deve_responder_400_apontando_o_id_quando_ele_nao_e_numero() throws Exception {
        // Agir e Verificar
        mvc.perform(get("/api/v1/consumidores/abc")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.id[0]").value("não tem o formato esperado"));
    }

    @Test
    void deve_responder_405_com_allow_quando_o_metodo_nao_e_get_nem_put() throws Exception {
        // Agir e Verificar
        mvc.perform(post("/api/v1/consumidores/" + id).contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"));
    }
}
