package br.com.alelo.consumer.consumerpat.api.comum.documentacao;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * O contrato da especificação OpenAPI publicada, com o contexto inteiro: o que um cliente que a
 * leia vai encontrar, e que não pode divergir do que a API faz.
 */
@SpringBootTest
@AutoConfigureMockMvc
class EspecificacaoDaApiTeste {

    private static final String PROBLEMA = "application/problem+json";

    /**
     * Os status de erro que têm o corpo de problema. O 503 da saúde tem o corpo da própria saúde.
     */
    private static final String ERROS_COM_PROBLEMA = "$.paths.*.*.responses['400','404','409',"
            + "'415','422','500'].content";

    @Autowired
    private MockMvc mvc;

    /** Lê como UTF-8, que é o do JSON: sem o charset no cabeçalho, o MockMvc leria ISO-8859-1. */
    private String especificacao() throws Exception {
        return mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void deve_trazer_o_titulo_a_descricao_e_a_versao_quando_ler_o_cabecalho() throws Exception {
        // Agir
        var corpo = especificacao();

        // Verificar
        assertThat((String) JsonPath.read(corpo, "$.info.title"))
                .isEqualTo(InformacoesDaApi.TITULO);
        assertThat((String) JsonPath.read(corpo, "$.info.description")).contains("RFC 7807");
        assertThat((String) JsonPath.read(corpo, "$.info.version")).isNotBlank();
    }

    @Test
    void deve_agrupar_as_operacoes_em_um_grupo_por_recurso_quando_ler_as_tags() throws Exception {
        // Agir
        var corpo = especificacao();
        List<String> grupos = JsonPath.read(corpo, "$.tags[*].name");
        List<List<String>> gruposDasOperacoes = JsonPath.read(corpo, "$.paths.*.*.tags");

        // Verificar: os três grupos declarados, e cada operação em exatamente um deles
        assertThat(grupos).containsExactly(TagsDaApi.CONSUMIDORES, TagsDaApi.CARTOES,
                TagsDaApi.SAUDE);
        assertThat(gruposDasOperacoes).hasSize(7)
                .allSatisfy(tags -> assertThat(tags).hasSize(1).first().isIn(grupos));
    }

    @Test
    void deve_resumir_toda_operacao_quando_ler_as_rotas() throws Exception {
        // Agir
        List<String> resumos = JsonPath.read(especificacao(), "$.paths.*.*.summary");

        // Verificar
        assertThat(resumos).hasSize(7).allSatisfy(resumo -> assertThat(resumo).isNotBlank());
    }

    @Test
    void deve_declarar_201_e_nao_200_quando_a_operacao_cria_alguma_coisa() throws Exception {
        // Agir: a especificação antiga dizia 200 nos três POST, que respondem 201
        List<Map<String, Object>> respostasDosPosts = JsonPath.read(especificacao(),
                "$.paths.*.post.responses");

        // Verificar
        assertThat(respostasDosPosts).hasSize(3).allSatisfy(
                respostas -> assertThat(respostas).containsKey("201").doesNotContainKey("200"));
    }

    @Test
    void deve_dar_o_corpo_de_problema_a_todo_erro_quando_ler_as_respostas() throws Exception {
        // Agir
        List<Map<String, Object>> corposDeErro = JsonPath.read(especificacao(), ERROS_COM_PROBLEMA);

        // Verificar: 400, 404, 409, 415, 422 e 500, em todas as operações que os declaram
        assertThat(corposDeErro).hasSizeGreaterThanOrEqualTo(7)
                .allSatisfy(corpo -> assertThat(corpo).containsOnlyKeys(PROBLEMA));
    }

    @Test
    void deve_declarar_a_falha_inesperada_em_toda_operacao_quando_ler_as_respostas()
            throws Exception {
        // Agir
        List<Map<String, Object>> falhas = JsonPath.read(especificacao(),
                "$.paths.*.*.responses.500");

        // Verificar
        assertThat(falhas).hasSize(7);
    }

    @Test
    void deve_declarar_o_x_request_id_em_toda_resposta_quando_ler_os_cabecalhos() throws Exception {
        // Agir
        List<Map<String, Object>> cabecalhos = JsonPath.read(especificacao(),
                "$.paths.*.*.responses.*.headers");

        // Verificar: cada resposta traz o seu, e nenhuma fica sem
        assertThat(cabecalhos).hasSizeGreaterThanOrEqualTo(7 * 2)
                .allSatisfy(resposta -> assertThat(resposta).containsKey("X-Request-Id"));
    }

    @Test
    void deve_descrever_o_corpo_das_respostas_e_do_erro_quando_ler_os_esquemas() throws Exception {
        // Agir
        Map<String, Object> esquemas = JsonPath.read(especificacao(), "$.components.schemas");

        // Verificar
        assertThat(esquemas).containsKeys("MensagemProblema", "MensagemConsumidor",
                "MensagemPagina", "MensagemComprovanteCompra", "MensagemComprovanteCredito",
                "MensagemSaude");
    }
}
