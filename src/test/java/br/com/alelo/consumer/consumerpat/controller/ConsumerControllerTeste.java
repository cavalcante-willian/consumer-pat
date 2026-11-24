package br.com.alelo.consumer.consumerpat.controller;

import br.com.alelo.consumer.consumerpat.entity.Consumer;
import br.com.alelo.consumer.consumerpat.respository.ConsumerRepository;
import br.com.alelo.consumer.consumerpat.respository.ExtractRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Caracteriza as regras do enunciado contra o contrato legado (rotas {@code /consumer/*}).
 *
 * <p>O oráculo são os comentários do controller, não o comportamento do legado: onde os dois
 * divergem, o teste afirma a regra. Cada teste cria o próprio consumidor, com números de cartão
 * exclusivos, para não depender de dado deixado por outro.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ConsumerControllerTeste {

    private static final AtomicInteger PROXIMO_NUMERO = new AtomicInteger(1000);

    private static final int ALIMENTACAO = 1;
    private static final int FARMACIA = 2;
    private static final int COMBUSTIVEL = 3;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ConsumerRepository consumidores;

    @Autowired
    ExtractRepository extratos;

    private Consumer consumidorComSaldoEmTodosOsCartoes(double saldo) {
        var consumidor = new Consumer();
        consumidor.setName("Consumidor " + PROXIMO_NUMERO.get());
        consumidor.setDocumentNumber(PROXIMO_NUMERO.incrementAndGet());
        consumidor.setFoodCardNumber(PROXIMO_NUMERO.incrementAndGet());
        consumidor.setFuelCardNumber(PROXIMO_NUMERO.incrementAndGet());
        consumidor.setDrugstoreNumber(PROXIMO_NUMERO.incrementAndGet());
        consumidor.setFoodCardBalance(saldo);
        consumidor.setFuelCardBalance(saldo);
        consumidor.setDrugstoreCardBalance(saldo);
        return consumidores.save(consumidor);
    }

    private Consumer recarregar(Consumer consumidor) {
        return consumidores.findById(consumidor.getId()).orElseThrow();
    }

    private MockHttpServletRequestBuilder compra(int tipoDoEstabelecimento, int cartao, double valor) {
        return get("/consumer/buy")
            .param("establishmentType", String.valueOf(tipoDoEstabelecimento))
            .param("establishmentName", "Estabelecimento")
            .param("cardNumber", String.valueOf(cartao))
            .param("productDescription", "Produto")
            .param("value", String.valueOf(valor));
    }

    private void comprar(int tipoDoEstabelecimento, int cartao, double valor) throws Exception {
        mockMvc.perform(compra(tipoDoEstabelecimento, cartao, valor)).andReturn();
    }

    private void creditar(int cartao, double valor) throws Exception {
        mockMvc.perform(get("/consumer/setcardbalance")
                .param("cardNumber", String.valueOf(cartao))
                .param("value", String.valueOf(valor)))
            .andReturn();
    }

    @Test
    void deve_cobrar_noventa_por_cento_quando_compra_no_cartao_de_alimentacao() throws Exception {
        var consumidor = consumidorComSaldoEmTodosOsCartoes(200.0);

        comprar(ALIMENTACAO, consumidor.getFoodCardNumber(), 100.0);

        assertThat(recarregar(consumidor).getFoodCardBalance()).isEqualTo(110.0);
    }

    @Test
    void deve_cobrar_cento_e_trinta_e_cinco_por_cento_quando_compra_no_cartao_de_combustivel() throws Exception {
        var consumidor = consumidorComSaldoEmTodosOsCartoes(200.0);

        comprar(COMBUSTIVEL, consumidor.getFuelCardNumber(), 100.0);

        assertThat(recarregar(consumidor).getFuelCardBalance()).isEqualTo(65.0);
    }

    @Test
    void deve_cobrar_o_valor_integral_quando_compra_no_cartao_de_farmacia() throws Exception {
        var consumidor = consumidorComSaldoEmTodosOsCartoes(200.0);

        comprar(FARMACIA, consumidor.getDrugstoreNumber(), 100.0);

        assertThat(recarregar(consumidor).getDrugstoreCardBalance()).isEqualTo(100.0);
    }

    @Test
    void deve_debitar_so_o_cartao_do_tipo_do_estabelecimento_quando_compra_no_cartao_de_alimentacao() throws Exception {
        var consumidor = consumidorComSaldoEmTodosOsCartoes(200.0);

        comprar(ALIMENTACAO, consumidor.getFoodCardNumber(), 100.0);

        var depois = recarregar(consumidor);
        assertThat(depois.getFuelCardBalance()).isEqualTo(200.0);
        assertThat(depois.getDrugstoreCardBalance()).isEqualTo(200.0);
    }

    @Test
    void deve_registrar_no_extrato_o_valor_efetivamente_debitado_quando_compra_no_cartao_de_alimentacao() throws Exception {
        var consumidor = consumidorComSaldoEmTodosOsCartoes(200.0);
        var cartao = consumidor.getFoodCardNumber();

        comprar(ALIMENTACAO, cartao, 100.0);

        var lancamentos = extratos.findAll().stream()
            .filter(e -> e.getCardNumber() == cartao)
            .toList();
        assertThat(lancamentos).hasSize(1);
        assertThat(lancamentos.get(0).getAmount()).isEqualTo(90.0);
    }

    @Test
    void deve_somar_ao_saldo_quando_creditar_no_cartao_de_alimentacao() throws Exception {
        var consumidor = consumidorComSaldoEmTodosOsCartoes(200.0);

        creditar(consumidor.getFoodCardNumber(), 50.0);

        assertThat(recarregar(consumidor).getFoodCardBalance()).isEqualTo(250.0);
    }

    @Test
    void deve_somar_ao_saldo_quando_creditar_no_cartao_de_combustivel() throws Exception {
        var consumidor = consumidorComSaldoEmTodosOsCartoes(200.0);

        creditar(consumidor.getFuelCardNumber(), 50.0);

        assertThat(recarregar(consumidor).getFuelCardBalance()).isEqualTo(250.0);
    }

    @Test
    void deve_somar_ao_saldo_quando_creditar_no_cartao_de_farmacia() throws Exception {
        var consumidor = consumidorComSaldoEmTodosOsCartoes(200.0);

        creditar(consumidor.getDrugstoreNumber(), 50.0);

        assertThat(recarregar(consumidor).getDrugstoreCardBalance()).isEqualTo(250.0);
    }

    @Test
    void deve_persistir_o_consumidor_quando_cadastrar() throws Exception {
        var documento = PROXIMO_NUMERO.incrementAndGet();
        var json = "{\"name\": \"Cadastrado " + documento + "\", \"documentNumber\": " + documento
            + ", \"email\": \"cadastrado@exemplo.com\", \"city\": \"São Paulo\"}";

        mockMvc.perform(post("/consumer/createConsumer").contentType("application/json").content(json))
            .andExpect(status().isOk());

        var cadastrado = consumidores.findAll().stream()
            .filter(c -> c.getDocumentNumber() == documento)
            .toList();
        assertThat(cadastrado).hasSize(1);
        assertThat(cadastrado.get(0).getName()).isEqualTo("Cadastrado " + documento);
        assertThat(cadastrado.get(0).getCity()).isEqualTo("São Paulo");
    }

    @Test
    void deve_listar_o_consumidor_quando_listar_todos() throws Exception {
        var consumidor = consumidorComSaldoEmTodosOsCartoes(200.0);

        mockMvc.perform(get("/consumer/consumerList"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(consumidor.getName())));
    }

    @Test
    void deve_atualizar_os_dados_cadastrais_quando_atualizar() throws Exception {
        var consumidor = consumidorComSaldoEmTodosOsCartoes(200.0);
        var json = """
            {"id": %d, "name": "Nome novo", "documentNumber": %d,
             "foodCardNumber": %d, "foodCardBalance": 200.0,
             "fuelCardNumber": %d, "fuelCardBalance": 200.0,
             "drugstoreNumber": %d, "drugstoreCardBalance": 200.0}
            """.formatted(consumidor.getId(), consumidor.getDocumentNumber(), consumidor.getFoodCardNumber(),
            consumidor.getFuelCardNumber(), consumidor.getDrugstoreNumber());

        mockMvc.perform(post("/consumer/updateConsumer").contentType("application/json").content(json))
            .andExpect(status().isOk());

        assertThat(recarregar(consumidor).getName()).isEqualTo("Nome novo");
    }

    @Test
    @Disabled("Defeito do legado: o crédito em cartão inexistente vira NullPointerException (resposta "
        + "500) em vez de uma resposta de cliente. Habilitar no commit que corrige.")
    void deve_recusar_o_credito_quando_o_cartao_nao_existe() throws Exception {
        mockMvc.perform(get("/consumer/setcardbalance")
                .param("cardNumber", "987654321")
                .param("value", "10.0"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @Disabled("Defeito do legado: o valor da compra não é validado, e um valor negativo AUMENTA o saldo "
        + "(a compra de -50,0 soma 50,0). Habilitar no commit que corrige.")
    void deve_recusar_a_compra_quando_o_valor_nao_e_positivo() throws Exception {
        var consumidor = consumidorComSaldoEmTodosOsCartoes(200.0);

        mockMvc.perform(compra(FARMACIA, consumidor.getDrugstoreNumber(), -50.0))
            .andExpect(status().is4xxClientError());

        assertThat(recarregar(consumidor).getDrugstoreCardBalance()).isEqualTo(200.0);
    }

    @Test
    @Disabled("Defeito do legado: o valor do crédito não é validado, e um crédito negativo DIMINUI o "
        + "saldo. Habilitar no commit que corrige.")
    void deve_recusar_o_credito_quando_o_valor_nao_e_positivo() throws Exception {
        var consumidor = consumidorComSaldoEmTodosOsCartoes(200.0);

        mockMvc.perform(get("/consumer/setcardbalance")
                .param("cardNumber", String.valueOf(consumidor.getDrugstoreNumber()))
                .param("value", "-50.0"))
            .andExpect(status().is4xxClientError());

        assertThat(recarregar(consumidor).getDrugstoreCardBalance()).isEqualTo(200.0);
    }

    @Test
    @Disabled("Defeito do legado: qualquer tipo de estabelecimento diferente de 1 e 2 cai no ramo do "
        + "combustível (o tipo 9 cobra 135% do cartão de combustível). Habilitar no commit que corrige.")
    void deve_recusar_a_compra_quando_o_tipo_de_estabelecimento_e_desconhecido() throws Exception {
        var consumidor = consumidorComSaldoEmTodosOsCartoes(200.0);

        mockMvc.perform(compra(9, consumidor.getFuelCardNumber(), 100.0))
            .andExpect(status().is4xxClientError());

        assertThat(recarregar(consumidor).getFuelCardBalance()).isEqualTo(200.0);
    }

    @Test
    @Disabled("Defeito do legado: updateConsumer grava o objeto recebido, inclusive o saldo, contra "
        + "a regra 'não deve ser possível alterar o saldo do cartão'. Habilitar no commit que corrige.")
    void deve_preservar_o_saldo_quando_atualizar_dados_cadastrais() throws Exception {
        var consumidor = consumidorComSaldoEmTodosOsCartoes(200.0);
        var json = """
            {"id": %d, "name": "Nome novo", "documentNumber": %d,
             "foodCardNumber": %d, "foodCardBalance": 999999.0,
             "fuelCardNumber": %d, "fuelCardBalance": 999999.0,
             "drugstoreNumber": %d, "drugstoreCardBalance": 999999.0}
            """.formatted(consumidor.getId(), consumidor.getDocumentNumber(), consumidor.getFoodCardNumber(),
            consumidor.getFuelCardNumber(), consumidor.getDrugstoreNumber());

        mockMvc.perform(post("/consumer/updateConsumer").contentType("application/json").content(json)).andReturn();

        var depois = recarregar(consumidor);
        assertThat(depois.getName()).isEqualTo("Nome novo");
        assertThat(depois.getFoodCardBalance()).isEqualTo(200.0);
        assertThat(depois.getFuelCardBalance()).isEqualTo(200.0);
        assertThat(depois.getDrugstoreCardBalance()).isEqualTo(200.0);
    }

    @Test
    @Disabled("Defeito do legado: a compra subtrai sem conferir o saldo e deixa o cartão negativo "
        + "(o saldo de 10,0 vira -90,0). Habilitar no commit que corrige.")
    void deve_recusar_a_compra_quando_o_saldo_e_insuficiente() throws Exception {
        var consumidor = consumidorComSaldoEmTodosOsCartoes(10.0);

        comprar(FARMACIA, consumidor.getDrugstoreNumber(), 100.0);

        assertThat(recarregar(consumidor).getDrugstoreCardBalance()).isEqualTo(10.0);
    }

    @Test
    @Disabled("Defeito do legado: o cartão do tipo errado não é achado e o controller lança "
        + "NullPointerException (resposta 500) em vez de recusar a compra. Habilitar no commit que corrige.")
    void deve_recusar_a_compra_quando_o_tipo_do_cartao_difere_do_estabelecimento() throws Exception {
        var consumidor = consumidorComSaldoEmTodosOsCartoes(200.0);

        mockMvc.perform(compra(ALIMENTACAO, consumidor.getDrugstoreNumber(), 100.0))
            .andExpect(status().is4xxClientError());

        assertThat(recarregar(consumidor).getDrugstoreCardBalance()).isEqualTo(200.0);
    }

    @Test
    @Disabled("Defeito do legado: cartão inexistente vira NullPointerException (resposta 500) em vez "
        + "de uma resposta de cliente. Habilitar no commit que corrige.")
    void deve_recusar_a_compra_quando_o_cartao_nao_existe() throws Exception {
        mockMvc.perform(compra(ALIMENTACAO, 987654321, 10.0))
            .andExpect(status().is4xxClientError());
    }
}
