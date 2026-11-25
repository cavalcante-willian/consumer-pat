package br.com.alelo.consumer.consumerpat.api.consumidor.criar;

import br.com.alelo.consumer.consumerpat.api.ConfiguracaoDeCasosDeUsoComFakes;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.consumidor.ConsumidorPortaFake;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeConsumidores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CriarConsumidorController.class)
@Import(ConfiguracaoDeCasosDeUsoComFakes.class)
class CriarConsumidorControllerTeste {

    private static final String ROTA = "/api/v1/consumidores";
    private static final MediaType PROBLEMA = MediaType.valueOf("application/problem+json");

    private static final String DADOS = """
            {"nome":"Maria Souza","documento":"%s","dataNascimento":"1990-05-17",
            "celular":"(11) 99999-8888","email":"maria@exemplo.com.br",
            "rua":"Rua das Flores","numero":"12-B","cidade":"São Paulo","pais":"Brasil",
            "cep":"01001-000"}""";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ConsumidorPortaFake consumidores;

    private static String corpo(String documento, String cartoes) {
        return "{\"dados\":" + DADOS.formatted(documento) + ",\"numerosCartoes\":" + cartoes + "}";
    }

    @BeforeEach
    void limpar() {
        consumidores.memoria().consumidores().clear();
        consumidores.memoria().gravacoes().clear();
    }

    @Test
    void deve_responder_201_com_location_e_os_dados_mascarados_quando_o_cadastro_e_valido()
            throws Exception {
        // Agir e Verificar
        var resposta = mvc
                .perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("123.456.789-01", "{\"ALIMENTACAO\":\"5555000011112222\"}")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(ROTA + "/\\d+")))
                .andExpect(jsonPath("$.versao").value(0))
                .andExpect(jsonPath("$.documento").value("****8901"))
                .andExpect(jsonPath("$.celular").value("****8888"))
                .andExpect(jsonPath("$.cartoes[0].numero").value("****2222"))
                .andExpect(jsonPath("$.cartoes[0].saldo").value("0.000000")).andReturn()
                .getResponse();

        // Verificar: o Location aponta para o id que o corpo devolveu
        var id = consumidores.gravados().get(0).id().valor();
        assertThat(resposta.getHeader("Location")).isEqualTo(ROTA + "/" + id);
    }

    @Test
    void deve_gravar_o_consumidor_com_o_cartao_de_saldo_zero_quando_o_cadastro_e_valido()
            throws Exception {
        // Agir
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("12345678901", "{\"FARMACIA\":\"5555000011113333\"}")))
                .andExpect(status().isCreated());

        // Verificar
        assertThat(consumidores.gravados()).hasSize(1);
        assertThat(consumidores.gravados().get(0).cartoes().cartoes()).hasSize(1);
    }

    @Test
    void deve_nunca_devolver_o_documento_nem_o_numero_inteiros_quando_o_cadastro_e_valido()
            throws Exception {
        // Agir e Verificar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("12345678901", "{\"ALIMENTACAO\":\"5555000011112222\"}")))
                .andExpect(content().string(not(containsString("12345678901"))))
                .andExpect(content().string(not(containsString("5555000011112222"))));
    }

    @Test
    void deve_aceitar_o_cadastro_sem_cartoes_quando_o_mapa_e_vazio() throws Exception {
        // Agir e Verificar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("12345678901", "{}"))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.cartoes").isEmpty());
    }

    @Test
    void deve_reunir_todos_os_erros_de_conversao_de_uma_vez_quando_varios_campos_sao_invalidos()
            throws Exception {
        // Agir e Verificar: documento, CEP, tipo de cartão e número, tudo errado ao mesmo tempo
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("123", "{\"REFEICAO\":\"1\"}").replace("01001-000", "12")))
                .andExpect(status().isBadRequest()).andExpect(content().contentType(PROBLEMA))
                .andExpect(jsonPath("$.type").value("urn:consumer-pat:erro:CPT-VAL-GER-001"))
                .andExpect(jsonPath("$.errors.documento").isArray())
                .andExpect(jsonPath("$.errors.cep").isArray())
                .andExpect(jsonPath("$.errors.numerosCartoes.length()").value(2));
        assertThat(consumidores.gravados()).isEmpty();
    }

    @Test
    void deve_responder_400_com_os_campos_ausentes_quando_o_bean_validation_recusa()
            throws Exception {
        // Agir e Verificar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content("{\"dados\":{},\"numerosCartoes\":{}}")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['dados.nome']").isArray())
                .andExpect(jsonPath("$.errors['dados.documento']").isArray());
    }

    @Test
    void deve_recusar_um_campo_de_saldo_quando_o_cadastro_tenta_definir_o_saldo() throws Exception {
        // Agir e Verificar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("12345678901", "{}").replace("\"numerosCartoes\"",
                        "\"saldo\":999,\"numerosCartoes\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.saldo[0]").value("não é aceito nesta operação"));
    }

    @Test
    void deve_responder_409_e_nao_gravar_quando_o_documento_ja_existe() throws Exception {
        // Preparar
        consumidores.criar(FabricaDeConsumidores.novo());

        // Agir e Verificar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(corpo(FabricaDeConsumidores.DOCUMENTO_PADRAO, "{}")))
                .andExpect(status().isConflict()).andExpect(content().contentType(PROBLEMA))
                .andExpect(jsonPath("$.type").value("urn:consumer-pat:erro:CPT-CNF-CON-001"))
                .andExpect(content()
                        .string(not(containsString(FabricaDeConsumidores.DOCUMENTO_PADRAO))));
    }
}
