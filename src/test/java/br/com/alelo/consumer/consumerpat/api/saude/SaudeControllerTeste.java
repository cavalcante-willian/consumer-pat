package br.com.alelo.consumer.consumerpat.api.saude;

import br.com.alelo.consumer.consumerpat.api.ConfiguracaoDeCasosDeUsoComFakes;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.SaudePortaFake;
import br.com.alelo.consumer.consumerpat.dominio.saude.EstadoDeDependencia;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SaudeController.class)
@Import(ConfiguracaoDeCasosDeUsoComFakes.class)
class SaudeControllerTeste {

    private static final String ROTA = "/api/v1/saude";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SaudePortaFake saude;

    @AfterEach
    void restaurar() {
        saude.banco(EstadoDeDependencia.DISPONIVEL);
    }

    @Test
    void deve_responder_200_quando_banco_disponivel() throws Exception {
        // Agir e Verificar
        mvc.perform(get(ROTA)).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.estado").value("SAUDAVEL"))
                .andExpect(jsonPath("$.dependencias.banco").value("DISPONIVEL"));
    }

    @Test
    void deve_responder_503_quando_banco_indisponivel() throws Exception {
        // Preparar
        saude.banco(EstadoDeDependencia.INDISPONIVEL);

        // Agir e Verificar: o único endpoint do projeto que responde 503
        mvc.perform(get(ROTA)).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.estado").value("DEGRADADO"))
                .andExpect(jsonPath("$.dependencias.banco").value("INDISPONIVEL"));
    }

    @Test
    void deve_ter_so_a_situacao_e_as_dependencias_quando_o_corpo_e_publico() throws Exception {
        // Agir e Verificar
        mvc.perform(get(ROTA)).andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.dependencias.length()").value(1));
    }

    @Test
    void deve_nao_expor_nada_interno_quando_o_banco_esta_indisponivel() throws Exception {
        // Preparar
        saude.banco(EstadoDeDependencia.INDISPONIVEL);

        // Agir e Verificar: nem URL, nem host, nem versão, nem exceção
        mvc.perform(get(ROTA)).andExpect(content().string(not(containsString("jdbc"))))
                .andExpect(content().string(not(containsString("h2"))))
                .andExpect(content().string(not(containsString("Exception"))))
                .andExpect(content().string(not(containsString("SQL"))));
    }

    @Test
    void deve_recusar_outro_metodo_quando_nao_e_get() throws Exception {
        // Agir e Verificar
        mvc.perform(post(ROTA)).andExpect(status().isMethodNotAllowed());
    }
}
