package br.com.alelo.consumer.consumerpat.api;

import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroDocumentoJaCadastrado;
import br.com.alelo.consumer.consumerpat.dominio.erros.regradenegocio.ErroSaldoInsuficiente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Percorre os eventos de negócio com o contexto inteiro, o banco real e o Logback de verdade: o que
 * o operador lê no console, com o id da requisição na mesma linha, e o que nunca pode estar nele.
 *
 * <p>
 * O banco é um só entre os testes da JVM, então cada teste usa o seu documento e o seu cartão.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class EventosDeNegocioDePontaAPontaTeste {

    private static final String ID_DO_PEDIDO = "9d2c1b6e-3f4a-4b8e-8c1d-5a7e2f0b9c11";

    @Autowired
    private MockMvc mvc;

    private static String cadastro(String documento, String numero) {
        return """
                {"dados":{"nome":"Maria","documento":"%s","dataNascimento":"1990-05-17",
                "celular":"11999998888","email":"maria@exemplo.com.br","rua":"Rua A",
                "numero":"1","cidade":"Recife","pais":"Brasil","cep":"50000000"},
                "numerosCartoes":{"ALIMENTACAO":"%s"}}""".formatted(documento, numero);
    }

    private void cadastrar(String documento, String numero, int statusEsperado) throws Exception {
        mvc.perform(post("/api/v1/consumidores").header("X-Request-Id", ID_DO_PEDIDO)
                .contentType(MediaType.APPLICATION_JSON).content(cadastro(documento, numero)))
                .andExpect(status().is(statusEsperado));
    }

    private void creditar(String numero, String valor) throws Exception {
        mvc.perform(post("/api/v1/creditos").header("X-Request-Id", ID_DO_PEDIDO)
                .contentType(MediaType.APPLICATION_JSON).content("{\"numeroCartao\":\"" + numero
                        + "\",\"tipoEstabelecimento\":1," + "\"valor\":" + valor + "}"))
                .andExpect(status().isCreated());
    }

    private int comprar(String numero, String valor) throws Exception {
        return mvc
                .perform(post("/api/v1/compras").header("X-Request-Id", ID_DO_PEDIDO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoEstabelecimento\":1,\"nomeEstabelecimento\":\"Mercado\","
                                + "\"numeroCartao\":\"" + numero + "\","
                                + "\"descricaoProduto\":\"Arroz\",\"valor\":" + valor + "}"))
                .andReturn().getResponse().getStatus();
    }

    @Test
    void deve_registrar_os_eventos_de_um_fluxo_completo_com_o_id_da_requisicao(
            CapturedOutput console) throws Exception {
        // Preparar
        var documento = "45317828791";
        var numero = "4532015112830366";

        // Agir
        cadastrar(documento, numero, 201);
        creditar(numero, "100.00");
        comprar(numero, "10.00");
        var recusada = comprar(numero, "100000.00");

        // Verificar: cada evento no nível certo, na mesma linha do id da requisição
        assertThat(recusada).isEqualTo(422);
        var linhas = console.getOut();
        assertThat(linhas).containsPattern(
                "INFO \\[" + ID_DO_PEDIDO + "\\] .* - consumidor.criado consumidorId=\\d+");
        assertThat(linhas).containsPattern("INFO \\[" + ID_DO_PEDIDO
                + "\\] .* - cartao.creditado cartao=\\*{4}0366 tipoCartao=ALIMENTACAO "
                + "valor=100.000000");
        assertThat(linhas).containsPattern("INFO \\[" + ID_DO_PEDIDO
                + "\\] .* - compra.registrada cartao=\\*{4}0366 tipoCartao=ALIMENTACAO "
                + "valorCobrado=9.000000");
        assertThat(linhas).containsPattern("INFO \\[" + ID_DO_PEDIDO
                + "\\] .* - compra.recusada cartao=\\*{4}0366 tipoCartao=ALIMENTACAO motivo="
                + ErroSaldoInsuficiente.CODIGO);
    }

    @Test
    void deve_registrar_o_conflito_em_aviso_sem_o_documento_quando_o_cadastro_repete(
            CapturedOutput console) throws Exception {
        // Preparar
        var documento = "62918374051";
        var numero = "4916338506082832";
        cadastrar(documento, numero, 201);

        // Agir
        cadastrar(documento, numero, 409);

        // Verificar
        assertThat(console.getOut()).containsPattern("WARN \\[" + ID_DO_PEDIDO
                + "\\] .* - conflito.detectado operacao=criar-consumidor codigo="
                + ErroDocumentoJaCadastrado.CODIGO);
    }

    @Test
    void deve_nao_deixar_o_numero_o_documento_nem_o_contato_chegarem_ao_console(
            CapturedOutput console) throws Exception {
        // Preparar
        var documento = "73829105462";
        var numero = "4024007148673576";

        // Agir: o fluxo inteiro, com sucesso, recusa e conflito
        cadastrar(documento, numero, 201);
        cadastrar(documento, numero, 409);
        creditar(numero, "100.00");
        comprar(numero, "10.00");
        comprar(numero, "100000.00");

        // Verificar: nem o número completo do cartão, nem o documento, nem o contato
        assertThat(console.getOut()).doesNotContain(numero).doesNotContain(documento)
                .doesNotContain("11999998888").doesNotContain("maria@exemplo.com.br");
    }
}
