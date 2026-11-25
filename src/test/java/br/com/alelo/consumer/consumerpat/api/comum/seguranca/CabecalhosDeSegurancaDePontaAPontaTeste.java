package br.com.alelo.consumer.consumerpat.api.comum.seguranca;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * O contrato dos cabeçalhos de segurança, com o contexto inteiro: toda resposta de sucesso e de
 * erro da API os traz, e as páginas que eles quebrariam, não.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CabecalhosDeSegurancaDePontaAPontaTeste {

    private static final String DOCUMENTO = "56473829104";

    private static final String NUMERO = "4485732019283746";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ApplicationContext contexto;

    private static String cadastro() {
        return """
                {"dados":{"nome":"Maria","documento":"%s","dataNascimento":"1990-05-17",
                "celular":"11999998888","email":"maria@exemplo.com.br","rua":"Rua A",
                "numero":"1","cidade":"Recife","pais":"Brasil","cep":"50000000"},
                "numerosCartoes":{"ALIMENTACAO":"%s"}}""".formatted(DOCUMENTO, NUMERO);
    }

    private static void comOsCabecalhos(ResultActions resposta) throws Exception {
        for (var cabecalho : CabecalhosDeSeguranca.PADRAO.entrySet()) {
            resposta.andExpect(header().string(cabecalho.getKey(), cabecalho.getValue()));
        }
    }

    private static void semOsCabecalhos(ResultActions resposta) throws Exception {
        resposta.andExpect(header().doesNotExist("X-Frame-Options"))
                .andExpect(header().doesNotExist("Content-Security-Policy"))
                .andExpect(header().doesNotExist("X-Content-Type-Options"));
    }

    @Test
    void deve_levar_os_cabecalhos_quando_a_resposta_e_de_sucesso() throws Exception {
        // Agir e Verificar: a leitura, o health e a criação, que são 200 e 201
        comOsCabecalhos(mvc.perform(get("/api/v1/consumidores")).andExpect(status().isOk()));
        comOsCabecalhos(mvc.perform(get("/api/v1/saude")).andExpect(status().isOk()));
        comOsCabecalhos(
                mvc.perform(post("/api/v1/consumidores").contentType(MediaType.APPLICATION_JSON)
                        .content(cadastro())).andExpect(status().isCreated()));
    }

    @Test
    void deve_levar_os_cabecalhos_quando_a_resposta_e_de_erro() throws Exception {
        // Preparar: o consumidor existe, para o conflito
        mvc.perform(post("/api/v1/consumidores").contentType(MediaType.APPLICATION_JSON).content(
                cadastro().replace(DOCUMENTO, "38291047562").replace(NUMERO, "4916338506082833")));

        // Agir e Verificar: cada status de erro do contrato, e todos application/problem+json
        var respostas = List.of(
                mvc.perform(get("/api/v1/consumidores?pagina=0"))
                        .andExpect(status().isBadRequest()),
                mvc.perform(get("/api/v1/nao-existe")).andExpect(status().isNotFound()),
                mvc.perform(request(HttpMethod.DELETE, "/api/v1/consumidores"))
                        .andExpect(status().isMethodNotAllowed()),
                mvc.perform(
                        post("/api/v1/consumidores").contentType(MediaType.TEXT_PLAIN).content("x"))
                        .andExpect(status().isUnsupportedMediaType()),
                mvc.perform(post("/api/v1/consumidores").contentType(MediaType.APPLICATION_JSON)
                        .content(cadastro().replace(DOCUMENTO, "38291047562").replace(NUMERO,
                                "4916338506082833")))
                        .andExpect(status().isConflict()),
                mvc.perform(post("/api/v1/compras").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoEstabelecimento\":1,\"nomeEstabelecimento\":\"Mercado\","
                                + "\"numeroCartao\":\"4916338506082833\","
                                + "\"descricaoProduto\":\"Arroz\",\"valor\":100000.00}"))
                        .andExpect(status().isUnprocessableEntity()));
        for (var resposta : respostas) {
            comOsCabecalhos(resposta);
            resposta.andExpect(header().exists("X-Request-Id"));
        }
    }

    @Test
    void deve_nao_levar_os_cabecalhos_quando_a_pagina_e_do_swagger_ou_do_console()
            throws Exception {
        // Agir e Verificar: a política de conteúdo e o DENY quebrariam essas páginas
        semOsCabecalhos(mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()));
        semOsCabecalhos(mvc.perform(get("/swagger-ui/index.html")));
        semOsCabecalhos(mvc.perform(get("/h2-console/")));
    }

    @Test
    void deve_manter_o_console_do_h2_no_perfil_padrao_porque_o_readme_o_documenta() {
        // Verificar: o perfil padrão é o ambiente de avaliação, e não é seguro para exposição
        assertThat(contexto.containsBean("h2Console")).isTrue();
    }

    @Test
    void deve_nao_enviar_cabecalho_de_cors_quando_a_origem_e_de_outro_site() throws Exception {
        // Agir e Verificar: sem cliente de origem conhecida, o padrão fechado é o estado desejado
        mvc.perform(get("/api/v1/consumidores").header("Origin", "https://outro.exemplo"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
        mvc.perform(options("/api/v1/consumidores").header("Origin", "https://outro.exemplo")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
