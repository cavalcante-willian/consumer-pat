package br.com.alelo.consumer.consumerpat.api.cartao.concorrencia;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.fabricas.CorposDaApi;
import br.com.alelo.consumer.consumerpat.fabricas.DocumentosDeTeste;
import br.com.alelo.consumer.consumerpat.fabricas.NumerosDeCartaoDeTeste;
import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * As chamadas à API que os testes de concorrência fazem, sempre pelo contexto inteiro: preparar um
 * cartão com saldo, creditar, comprar e ler o saldo de volta.
 *
 * <p>
 * Tudo passa pela API, inclusive a preparação e a leitura do saldo, para o teste provar o que um
 * cliente veria. Cada método devolve o status da resposta, que é o que a disputa confere.
 */
final class ChamadasDaApi {

    private static final String CONSUMIDORES = "/api/v1/consumidores";

    private static final String CREDITOS = "/api/v1/creditos";

    private static final String COMPRAS = "/api/v1/compras";

    private final MockMvc mvc;

    ChamadasDaApi(MockMvc mvc) {
        this.mvc = mvc;
    }

    /**
     * Um cartão pronto para a disputa: o número e onde ler o consumidor dono.
     *
     * @param numero      o número do cartão, só dígitos
     * @param localizacao o caminho do consumidor, que devolve o saldo
     */
    record CartaoPreparado(String numero, String localizacao) {
    }

    /**
     * Cadastra um consumidor novo com um cartão de farmácia e o credita com o saldo informado.
     *
     * @param saldo o saldo inicial, como decimal com ponto
     * @return o cartão preparado
     * @throws Exception se a preparação falha
     */
    CartaoPreparado prepararFarmaciaComSaldo(String saldo) throws Exception {
        var numero = NumerosDeCartaoDeTeste.proximo();
        var cadastro = mvc.perform(
                post(CONSUMIDORES).contentType(MediaType.APPLICATION_JSON).content(CorposDaApi
                        .cadastro(DocumentosDeTeste.proximo(), TipoCartao.FARMACIA, numero)))
                .andReturn().getResponse();
        assertThat(cadastro.getStatus()).as("cadastro do consumidor").isEqualTo(201);
        assertThat(creditar(numero, saldo)).as("crédito inicial").isEqualTo(201);
        return new CartaoPreparado(numero, cadastro.getHeader("Location"));
    }

    /**
     * Credita um cartão de farmácia.
     *
     * @param numero o número do cartão
     * @param valor  o valor, como decimal com ponto
     * @return o status da resposta
     * @throws Exception se a chamada falha
     */
    int creditar(String numero, String valor) throws Exception {
        return mvc
                .perform(post(CREDITOS).contentType(MediaType.APPLICATION_JSON)
                        .content(CorposDaApi.credito(numero, TipoCartao.FARMACIA, valor)))
                .andReturn().getResponse().getStatus();
    }

    /**
     * Compra num cartão de farmácia.
     *
     * @param numero o número do cartão
     * @param valor  o valor, como decimal com ponto
     * @return o status da resposta
     * @throws Exception se a chamada falha
     */
    int comprar(String numero, String valor) throws Exception {
        return mvc
                .perform(post(COMPRAS).contentType(MediaType.APPLICATION_JSON)
                        .content(CorposDaApi.compra(TipoCartao.FARMACIA, numero, valor)))
                .andReturn().getResponse().getStatus();
    }

    /**
     * Cadastra um consumidor com um cartão de farmácia de número novo e o documento informado.
     *
     * @param documento o documento, só dígitos
     * @return o status da resposta
     * @throws Exception se a chamada falha
     */
    int cadastrarComDocumento(String documento) throws Exception {
        return mvc
                .perform(post(CONSUMIDORES).contentType(MediaType.APPLICATION_JSON)
                        .content(CorposDaApi.cadastro(documento, TipoCartao.FARMACIA,
                                NumerosDeCartaoDeTeste.proximo())))
                .andReturn().getResponse().getStatus();
    }

    /**
     * Lê o saldo do cartão pelo consumidor dono, como um cliente leria.
     *
     * @param cartao o cartão preparado
     * @return o saldo, como decimal com ponto
     * @throws Exception se a leitura falha
     */
    String saldoDe(CartaoPreparado cartao) throws Exception {
        var corpo = mvc.perform(get(cartao.localizacao())).andReturn().getResponse()
                .getContentAsString();
        return JsonPath.read(corpo, "$.cartoes[0].saldo");
    }
}
