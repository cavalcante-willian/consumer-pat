package br.com.alelo.consumer.consumerpat.api.comum.seguranca;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prova o perfil {@code prod}: o console do H2 e o Swagger não existem, e os cabeçalhos de
 * segurança continuam em toda resposta da API. O destino do arquivo de log é trocado por um caminho
 * em {@code build}, para o teste não criar {@code logs/} na raiz do projeto.
 */
@SpringBootTest(properties = "logging.file.name=build/tmp/consumer-pat-prod-seguranca-teste.log")
@ActiveProfiles("prod")
@AutoConfigureMockMvc
class SegurancaDoPerfilDeProducaoTeste {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ApplicationContext contexto;

    @Autowired
    private Environment ambiente;

    @Test
    void deve_nao_registrar_o_console_do_h2_quando_perfil_prod() {
        // Verificar: o console com sa/sa contorna a API, a validação e a trava do saldo
        assertThat(contexto.containsBean("h2Console")).isFalse();
    }

    @Test
    void deve_manter_o_console_restrito_a_propria_maquina_quando_perfil_prod() {
        // Verificar: o padrão do Spring é falso, e nada o liga
        assertThat(ambiente.getProperty("spring.h2.console.settings.web-allow-others",
                Boolean.class, false)).isFalse();
    }

    @Test
    void deve_nao_servir_o_swagger_nem_a_especificacao_quando_perfil_prod() throws Exception {
        // Agir e Verificar: o contrato não precisa estar público em produção
        mvc.perform(get("/v3/api-docs")).andExpect(status().isNotFound());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isNotFound());
    }

    @Test
    void deve_manter_os_cabecalhos_de_seguranca_na_api_quando_perfil_prod() throws Exception {
        // Agir e Verificar
        mvc.perform(get("/api/v1/saude")).andExpect(status().isOk())
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Cache-Control", "no-store"));
    }
}
