package br.com.alelo.consumer.consumerpat.api.cartao.credito;

import br.com.alelo.consumer.consumerpat.api.ConfiguracaoDeCasosDeUsoComFakes;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.CartaoPortaFake;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.ChaveDeCartao;
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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CreditoController.class)
@Import(ConfiguracaoDeCasosDeUsoComFakes.class)
class CreditoControllerTeste {

    private static final String ROTA = "/api/v1/creditos";
    private static final String NUMERO = FabricaDeCartoes.NUMERO_PADRAO;
    private static final MediaType PROBLEMA = MediaType.valueOf("application/problem+json");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CartaoPortaFake cartoes;

    private static String corpo(String numero, int tipo, String valor) {
        return """
                {"numeroCartao":"%s","tipoEstabelecimento":%d,"valor":%s}""".formatted(numero, tipo,
                valor);
    }

    @BeforeEach
    void semear() {
        cartoes.memoria().cartoes().clear();
        cartoes.memoria().gravacoes().clear();
        var cartao = FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).construir();
        cartoes.memoria().cartoes()
                .put(new ChaveDeCartao(new NumeroDeCartao(NUMERO), TipoCartao.ALIMENTACAO), cartao);
    }

    @Test
    void deve_responder_201_com_o_saldo_novo_quando_o_credito_e_valido() throws Exception {
        // Agir e Verificar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(corpo(NUMERO, 1, "25.50"))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCartao").value("****2222"))
                .andExpect(jsonPath("$.tipo").value("ALIMENTACAO"))
                .andExpect(jsonPath("$.valorCreditado").value("25.500000"))
                .andExpect(jsonPath("$.saldoAtual").value("125.500000"));
    }

    @Test
    void deve_gravar_o_cartao_com_o_saldo_novo_quando_o_credito_e_valido() throws Exception {
        // Agir
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(corpo(NUMERO, 1, "25.50"))).andExpect(status().isCreated());

        // Verificar
        assertThat(cartoes.gravados()).hasSize(1);
    }

    @Test
    void deve_nunca_devolver_o_numero_inteiro_quando_o_credito_e_valido() throws Exception {
        // Agir e Verificar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(corpo(NUMERO, 1, "25.50")))
                .andExpect(content().string(not(containsString(NUMERO))));
    }

    @Test
    void deve_responder_404_quando_o_cartao_do_tipo_nao_existe() throws Exception {
        // Agir e Verificar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(corpo(NUMERO, 3, "10.00"))).andExpect(status().isNotFound())
                .andExpect(content().contentType(PROBLEMA));
        assertThat(cartoes.gravados()).isEmpty();
    }

    @Test
    void deve_responder_400_com_todos_os_campos_quando_a_mensagem_e_invalida() throws Exception {
        // Agir e Verificar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content("{\"valor\":0}"))
                .andExpect(status().isBadRequest()).andExpect(content().contentType(PROBLEMA))
                .andExpect(jsonPath("$.errors.numeroCartao").isArray())
                .andExpect(jsonPath("$.errors.tipoEstabelecimento").isArray())
                .andExpect(jsonPath("$.errors.valor").isArray());
    }

    @Test
    void deve_responder_400_quando_o_valor_tem_mais_de_duas_casas() throws Exception {
        // Agir e Verificar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(corpo(NUMERO, 1, "10.001"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.valor").isArray());
    }

    @Test
    void deve_responder_400_quando_o_tipo_de_estabelecimento_nao_existe() throws Exception {
        // Agir e Verificar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(corpo(NUMERO, 99, "10.00"))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.tipoEstabelecimento").isArray());
    }

    @Test
    void deve_responder_400_quando_o_valor_nao_e_numero() throws Exception {
        // Agir e Verificar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content("{\"numeroCartao\":\"" + NUMERO + "\",\"tipoEstabelecimento\":1,"
                        + "\"valor\":\"muito\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.valor[0]").value("não tem o formato esperado"))
                .andExpect(content().string(not(containsString("muito"))));
    }

    @Test
    void deve_responder_405_com_o_cabecalho_allow_quando_o_metodo_e_get() throws Exception {
        // Agir e Verificar
        mvc.perform(get(ROTA)).andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", "POST"));
    }
}
