package br.com.alelo.consumer.consumerpat.api.consumidor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Percorre a API do consumidor com o contexto inteiro e o H2 real, sem fake nenhum: o que os testes
 * de endpoint provam com a porta em memória, aqui é provado contra o banco, com as restrições, a
 * trava e a versão de linha de verdade.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ConsumidorDePontaAPontaTeste {

    private static final String ROTA = "/api/v1/consumidores";

    @Autowired
    private MockMvc mvc;

    private static String dados(String documento, String nome) {
        return """
                {"nome":"%s","documento":"%s","dataNascimento":"1990-05-17",
                "celular":"11999998888","email":"maria@exemplo.com.br","rua":"Rua A",
                "numero":"1","cidade":"Recife","pais":"Brasil","cep":"50000000"}""".formatted(nome,
                documento);
    }

    private static String cadastro(String documento, String cartoes) {
        return "{\"dados\":" + dados(documento, "Maria") + ",\"numerosCartoes\":" + cartoes + "}";
    }

    @Test
    void deve_cadastrar_ler_atualizar_e_recusar_a_versao_velha_quando_percorrer_o_fluxo()
            throws Exception {
        // Cadastrar
        var criado = mvc
                .perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                        .content(cadastro("39053344705", "{\"ALIMENTACAO\":\"4111111111111111\"}")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.versao").value(0))
                .andExpect(jsonPath("$.cartoes[0].saldo").value("0.000000")).andReturn()
                .getResponse();
        var localizacao = criado.getHeader("Location");
        assertThat(localizacao).startsWith(ROTA + "/");

        // Ler pelo Location
        mvc.perform(get(localizacao)).andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria"))
                .andExpect(jsonPath("$.documento").value("****4705"))
                .andExpect(jsonPath("$.cartoes[0].numero").value("****1111"));

        // Atualizar com a versão lida
        mvc.perform(put(localizacao).contentType(MediaType.APPLICATION_JSON)
                .content("{\"versao\":0,\"dados\":" + dados("39053344705", "Maria Nova") + "}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Maria Nova"))
                .andExpect(jsonPath("$.versao").value(1));

        // A versão velha é recusada
        mvc.perform(put(localizacao).contentType(MediaType.APPLICATION_JSON)
                .content("{\"versao\":0,\"dados\":" + dados("39053344705", "Outra") + "}"))
                .andExpect(status().isConflict());
    }

    @Test
    void deve_recusar_o_documento_repetido_com_409_quando_cadastrar_duas_vezes() throws Exception {
        // Preparar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(cadastro("52998224725", "{}"))).andExpect(status().isCreated());

        // Agir e Verificar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(cadastro("529.982.247-25", "{}"))).andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("urn:consumer-pat:erro:CPT-CNF-CON-001"));
    }

    @Test
    void deve_listar_so_id_e_nome_paginado_e_ordenado_quando_ha_consumidores_no_banco()
            throws Exception {
        // Preparar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(cadastro("71428793860", "{}"))).andExpect(status().isCreated());

        // Agir e Verificar: o contexto inteiro, com a transação só de leitura e o banco real
        mvc.perform(get(ROTA + "?pagina=1&tamanhoPagina=1&ordenacao=id&direcao=desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.itens[0].nome").value("Maria"))
                .andExpect(jsonPath("$.itens[0].documento").doesNotExist())
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.totalPaginas").isNumber());
    }

    @Test
    void deve_recusar_a_listagem_com_400_quando_os_parametros_sao_invalidos() throws Exception {
        // Agir e Verificar
        mvc.perform(get(ROTA + "?tamanhoPagina=101")).andExpect(status().isBadRequest());
    }

    @Test
    void deve_recusar_o_numero_de_cartao_repetido_com_409_e_nao_cadastrar_o_consumidor()
            throws Exception {
        // Preparar
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(cadastro("11144477735", "{\"FARMACIA\":\"4222222222222222\"}")))
                .andExpect(status().isCreated());

        // Agir e Verificar: outro consumidor tenta o mesmo número de cartão
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                .content(cadastro("86288366757", "{\"COMBUSTIVEL\":\"4222222222222222\"}")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("urn:consumer-pat:erro:CPT-CNF-CRT-001"));
    }
}
