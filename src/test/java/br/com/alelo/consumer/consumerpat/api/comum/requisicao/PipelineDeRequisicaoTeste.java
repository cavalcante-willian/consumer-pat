package br.com.alelo.consumer.consumerpat.api.comum.requisicao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prova o pipeline no contexto inteiro: a ordem dos filtros, o id em toda resposta e em todo erro,
 * e o id na linha de conclusão do log.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class PipelineDeRequisicaoTeste {

    private static final String UUID_DO_CLIENTE = "0f1c9c1e-8a45-4d5e-9f77-1a2b3c4d5e6f";

    @Autowired
    private MockMvc mvc;

    @Test
    void deve_devolver_o_id_de_requisicao_quando_qualquer_resposta() throws Exception {
        // Agir e Verificar: o sucesso, o 400 de validação e o 404 de rota
        mvc.perform(get("/api/v1/consumidores")).andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", org.hamcrest.Matchers.hasLength(36)));
        mvc.perform(get("/api/v1/consumidores?tamanhoPagina=0")).andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Request-Id"));
        mvc.perform(get("/api/v1/nao-existe")).andExpect(status().isNotFound())
                .andExpect(header().exists("X-Request-Id"));
    }

    @Test
    void deve_levar_o_mesmo_id_no_cabecalho_e_no_corpo_quando_a_resposta_e_um_erro()
            throws Exception {
        // Agir e Verificar
        mvc.perform(
                get("/api/v1/consumidores?tamanhoPagina=0").header("X-Request-Id", UUID_DO_CLIENTE))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Request-Id", UUID_DO_CLIENTE))
                .andExpect(jsonPath("$.idRequisicao").value(UUID_DO_CLIENTE));
    }

    @Test
    void deve_levar_o_id_no_corpo_de_erro_de_rota_inexistente() throws Exception {
        // Agir
        var resultado = mvc.perform(get("/api/v1/nao-existe")).andExpect(status().isNotFound())
                .andReturn().getResponse();

        // Verificar: o usuário reporta o id, e o operador acha o log
        assertThat(resultado.getContentAsString())
                .contains("\"idRequisicao\":\"" + resultado.getHeader("X-Request-Id") + "\"");
    }

    @Test
    void deve_omitir_o_id_do_corpo_quando_a_resposta_nao_e_erro() throws Exception {
        // Agir e Verificar
        mvc.perform(get("/api/v1/consumidores"))
                .andExpect(jsonPath("$.idRequisicao").doesNotExist());
    }

    @Test
    void deve_regerar_o_id_quando_o_do_cliente_tenta_forjar_uma_linha_de_log(CapturedOutput console)
            throws Exception {
        // Agir
        var resposta = mvc
                .perform(get("/api/v1/consumidores").header("X-Request-Id",
                        "forjado 2026-01-01 ERROR falso"))
                .andExpect(status().isOk()).andReturn().getResponse();

        // Verificar
        assertThat(resposta.getHeader("X-Request-Id")).hasSize(36).doesNotContain("forjado");
        assertThat(console.getOut()).doesNotContain("forjado");
    }

    @Test
    void deve_escrever_a_conclusao_com_o_id_quando_a_requisicao_termina(CapturedOutput console)
            throws Exception {
        // Agir
        mvc.perform(get("/api/v1/consumidores?pagina=1").header("X-Request-Id", UUID_DO_CLIENTE))
                .andExpect(status().isOk());

        // Verificar: a conclusão fica dentro da correlação, e por isso ainda tem o id
        assertThat(console.getOut()).containsPattern("INFO \\[" + UUID_DO_CLIENTE
                + "\\] .* - requisicao.concluida metodo=GET caminho=/api/v1/consumidores "
                + "status=200 duracaoMs=\\d+");
        assertThat(console.getOut()).doesNotContain("pagina=1");
    }

    @Test
    void deve_registrar_a_conclusao_de_erro_de_negocio_como_informacao(CapturedOutput console)
            throws Exception {
        // Agir
        mvc.perform(get("/api/v1/consumidores/999999").header("X-Request-Id", UUID_DO_CLIENTE))
                .andExpect(status().isNotFound());

        // Verificar: 404 é resultado esperado, e não erro do sistema
        assertThat(console.getOut()).containsPattern("INFO \\[" + UUID_DO_CLIENTE
                + "\\] .* - requisicao.concluida metodo=GET caminho=/api/v1/consumidores/999999 "
                + "status=404");
    }

    @Test
    void deve_limpar_o_mdc_quando_a_requisicao_termina() throws Exception {
        // Agir
        mvc.perform(get("/api/v1/consumidores")).andExpect(status().isOk());

        // Verificar: a thread do teste é a mesma, e nada ficou nela
        assertThat(MDC.get("idRequisicao")).isNull();
    }
}
