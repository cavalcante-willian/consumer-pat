package br.com.alelo.consumer.consumerpat.api.saude;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Percorre o health com o contexto inteiro e o H2 real: o pool de verdade entrega a conexão, e a
 * conexão passa na validação.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SaudeDePontaAPontaTeste {

    @Autowired
    private MockMvc mvc;

    @Test
    void deve_responder_200_saudavel_quando_o_banco_real_responde() throws Exception {
        // Agir e Verificar
        mvc.perform(get("/api/v1/saude")).andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("SAUDAVEL"))
                .andExpect(jsonPath("$.dependencias.banco").value("DISPONIVEL"));
    }

    @Test
    void deve_levar_o_id_de_requisicao_quando_o_health_responde() throws Exception {
        // Agir e Verificar: o health também passa pelo pipeline de correlação
        mvc.perform(get("/api/v1/saude")).andExpect(header().exists("X-Request-Id"));
    }
}
