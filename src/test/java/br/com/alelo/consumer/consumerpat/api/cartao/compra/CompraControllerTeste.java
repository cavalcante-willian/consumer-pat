package br.com.alelo.consumer.consumerpat.api.cartao.compra;

import br.com.alelo.consumer.consumerpat.api.ConfiguracaoDeCasosDeUsoComFakes;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.CartaoPortaFake;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.ChaveDeCartao;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.ExtratoPortaFake;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeCartoes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CompraController.class)
@Import(ConfiguracaoDeCasosDeUsoComFakes.class)
class CompraControllerTeste {

    private static final String ROTA = "/api/v1/compras";
    private static final String NUMERO = FabricaDeCartoes.NUMERO_PADRAO;
    private static final MediaType PROBLEMA = MediaType.valueOf("application/problem+json");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CartaoPortaFake cartoes;

    @Autowired
    private ExtratoPortaFake extratos;

    private static String corpo(int tipo, String numero, String valor) {
        return """
                {"tipoEstabelecimento":%d,"nomeEstabelecimento":"Mercado",\
                "numeroCartao":"%s","descricaoProduto":"Arroz","valor":%s}""".formatted(tipo,
                numero, valor);
    }

    @BeforeEach
    void semear() {
        cartoes.memoria().cartoes().clear();
        cartoes.memoria().gravacoes().clear();
        extratos.extratosEmMemoria().clear();
        var cartao = FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).construir();
        cartoes.memoria().cartoes()
                .put(new ChaveDeCartao(new NumeroDeCartao(NUMERO), TipoCartao.ALIMENTACAO), cartao);
    }

    @Test
    void deve_responder_201_com_o_comprovante_quando_a_compra_e_valida() throws Exception {
        // Agir e Verificar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(corpo(1, NUMERO, "10.00"))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCartao").value("****2222"))
                .andExpect(jsonPath("$.tipo").value("ALIMENTACAO"))
                .andExpect(jsonPath("$.valorInformado").value("10.000000"))
                .andExpect(jsonPath("$.instante").value("2026-11-24T12:00:00Z"));
    }

    @Test
    void deve_gravar_a_compra_e_o_extrato_quando_a_compra_e_valida() throws Exception {
        // Agir
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(corpo(1, NUMERO, "10.00"))).andExpect(status().isCreated());

        // Verificar
        assertThat(cartoes.gravados()).hasSize(1);
        assertThat(extratos.registrados()).hasSize(1);
    }

    @Test
    void deve_nunca_devolver_o_numero_inteiro_quando_a_compra_e_valida() throws Exception {
        // Agir e Verificar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(corpo(1, NUMERO, "10.00")))
                .andExpect(content().string(
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(NUMERO))));
    }

    @Test
    void deve_responder_422_quando_o_saldo_nao_cobre_a_compra() throws Exception {
        // Agir e Verificar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(corpo(1, NUMERO, "9999.00"))).andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(PROBLEMA))
                .andExpect(jsonPath("$.type").value("urn:consumer-pat:erro:CPT-REG-CRT-001"))
                .andExpect(jsonPath("$.instance").value(ROTA));
        assertThat(cartoes.gravados()).isEmpty();
    }

    @Test
    void deve_responder_404_quando_o_cartao_nao_existe() throws Exception {
        // Agir e Verificar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(corpo(2, NUMERO, "10.00"))).andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEMA));
    }

    @Test
    void deve_responder_400_com_todos_os_campos_quando_a_mensagem_e_invalida() throws Exception {
        // Agir e Verificar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipoEstabelecimento\":1,\"valor\":-1}"))
                .andExpect(status().isBadRequest()).andExpect(content().contentType(PROBLEMA))
                .andExpect(jsonPath("$.errors.numeroCartao").isArray())
                .andExpect(jsonPath("$.errors.nomeEstabelecimento").isArray())
                .andExpect(jsonPath("$.errors.valor").isArray());
    }

    @Test
    void deve_responder_400_sem_o_valor_rejeitado_quando_o_numero_nao_tem_o_formato()
            throws Exception {
        // Agir e Verificar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(corpo(1, "12345678", "10.00"))).andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers
                        .not(org.hamcrest.Matchers.containsString("12345678"))));
    }

    @Test
    void deve_responder_400_quando_o_corpo_traz_campo_desconhecido() throws Exception {
        // Agir e Verificar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(corpo(1, NUMERO, "10.00").replace("}", ",\"saldo\":1}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.saldo[0]").value("não é aceito nesta operação"));
    }

    @Test
    void deve_responder_400_quando_o_corpo_e_um_json_malformado() throws Exception {
        // Agir e Verificar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content("{\"valor\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.corpo[0]").value("não é um JSON válido"));
    }

    @Test
    void deve_responder_400_dizendo_que_o_corpo_e_obrigatorio_quando_nao_ha_corpo()
            throws Exception {
        // Agir e Verificar: protege a marca do texto do Spring, que o código reconhece
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.corpo[0]").value("é obrigatório"));
    }

    @Test
    void deve_responder_415_quando_a_midia_nao_e_json() throws Exception {
        // Agir e Verificar
        mvc.perform(post(ROTA).contentType(MediaType.TEXT_PLAIN).content("x"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.type").value("urn:consumer-pat:erro:CPT-VAL-SIS-003"));
    }

    @Test
    void deve_responder_405_com_o_cabecalho_allow_quando_o_metodo_e_get() throws Exception {
        // Agir e Verificar
        mvc.perform(get(ROTA)).andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", "POST"))
                .andExpect(jsonPath("$.type").value("urn:consumer-pat:erro:CPT-VAL-SIS-002"));
    }

    @Test
    void deve_responder_404_de_problema_quando_a_rota_nao_existe() throws Exception {
        // Agir e Verificar
        mvc.perform(get("/api/v1/inexistente")).andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEMA))
                .andExpect(jsonPath("$.type").value("urn:consumer-pat:erro:CPT-NEN-SIS-001"));
    }
}
