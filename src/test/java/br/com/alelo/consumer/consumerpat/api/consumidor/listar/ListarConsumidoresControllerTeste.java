package br.com.alelo.consumer.consumerpat.api.consumidor.listar;

import br.com.alelo.consumer.consumerpat.api.ConfiguracaoDeCasosDeUsoComFakes;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.consumidor.ConsumidorPortaFake;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.CartoesDoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.DadosDoConsumidor;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeConsumidores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ListarConsumidoresController.class)
@Import(ConfiguracaoDeCasosDeUsoComFakes.class)
class ListarConsumidoresControllerTeste {

    private static final MediaType PROBLEMA = MediaType.valueOf("application/problem+json");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ConsumidorPortaFake consumidores;

    private static Consumidor consumidor(long id, String nome) {
        var base = FabricaDeConsumidores.dadosComDocumento(String.format("%011d", id));
        var dados = new DadosDoConsumidor(nome, base.documento(), base.dataNascimento(),
                base.contato(), base.endereco());
        return new Consumidor(new ConsumidorId(id), dados, new CartoesDoConsumidor(List.of()), 0);
    }

    @BeforeEach
    void semear() {
        consumidores.memoria().consumidores().clear();
        List.of(consumidor(1, "Carla"), consumidor(2, "Ana"), consumidor(3, "Bruno"))
                .forEach(c -> consumidores.memoria().consumidores().put(c.id(), c));
    }

    @Test
    void deve_usar_os_padroes_quando_nao_ha_parametro() throws Exception {
        // Agir e Verificar: página 1, 20 por página, nome crescente
        mvc.perform(get("/api/v1/consumidores")).andExpect(status().isOk())
                .andExpect(jsonPath("$.pagina").value(1))
                .andExpect(jsonPath("$.tamanhoPagina").value(20))
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.totalPaginas").value(1))
                .andExpect(jsonPath("$.itens[0].nome").value("Ana"))
                .andExpect(jsonPath("$.itens[2].nome").value("Carla"));
    }

    @Test
    void deve_devolver_so_o_id_e_o_nome_quando_listar() throws Exception {
        // Agir e Verificar
        mvc.perform(get("/api/v1/consumidores")).andExpect(jsonPath("$.itens[0].id").value(2))
                .andExpect(jsonPath("$.itens[0].documento").doesNotExist())
                .andExpect(jsonPath("$.itens[0].cartoes").doesNotExist())
                .andExpect(content().string(not(containsString("12345678901"))));
    }

    @Test
    void deve_aceitar_direcao_e_ordenacao_em_maiusculas_quando_pedidas() throws Exception {
        // Agir e Verificar
        mvc.perform(get("/api/v1/consumidores?ordenacao=ID&direcao=DESC"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.itens[0].id").value(3));
    }

    @Test
    void deve_cortar_a_pagina_quando_o_tamanho_e_menor_que_o_total() throws Exception {
        // Agir e Verificar
        mvc.perform(get("/api/v1/consumidores?pagina=2&tamanhoPagina=2")).andExpect(status().isOk())
                .andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.itens[0].nome").value("Carla"))
                .andExpect(jsonPath("$.totalPaginas").value(2));
    }

    @Test
    void deve_devolver_200_com_itens_vazios_quando_a_pagina_esta_alem_da_ultima() throws Exception {
        // Agir e Verificar
        mvc.perform(get("/api/v1/consumidores?pagina=99")).andExpect(status().isOk())
                .andExpect(jsonPath("$.itens.length()").value(0))
                .andExpect(jsonPath("$.total").value(3));
    }

    @Test
    void deve_responder_400_com_todos_os_campos_quando_todos_os_parametros_sao_invalidos()
            throws Exception {
        // Agir e Verificar
        mvc.perform(
                get("/api/v1/consumidores?pagina=0&tamanhoPagina=500&ordenacao=xyz&direcao=cima"))
                .andExpect(status().isBadRequest()).andExpect(content().contentType(PROBLEMA))
                .andExpect(jsonPath("$.errors.pagina").exists())
                .andExpect(jsonPath("$.errors.tamanhoPagina").exists())
                .andExpect(jsonPath("$.errors.ordenacao").exists())
                .andExpect(jsonPath("$.errors.direcao").exists());
    }

    @Test
    void deve_responder_400_e_nao_cortar_no_teto_quando_o_tamanho_passa_de_cem() throws Exception {
        // Agir e Verificar
        mvc.perform(get("/api/v1/consumidores?tamanhoPagina=101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.tamanhoPagina").exists());
    }

    @Test
    void deve_responder_400_sem_ecoar_o_valor_quando_a_ordenacao_e_um_nome_de_coluna()
            throws Exception {
        // Agir e Verificar: o cliente nunca escolhe uma coluna
        mvc.perform(get("/api/v1/consumidores?ordenacao=documento"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString("documento\""))));
    }
}
