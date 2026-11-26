package br.com.alelo.consumer.consumerpat.api.cartao.compra;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.fabricas.CorposDaApi;
import br.com.alelo.consumer.consumerpat.fabricas.DocumentosDeTeste;
import br.com.alelo.consumer.consumerpat.fabricas.NumerosDeCartaoDeTeste;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A regra do enunciado, de ponta a ponta: o preço de cada tipo de cartão, o saldo que sobra e o que
 * o extrato guarda, com o contexto inteiro e o H2 real.
 *
 * <p>
 * É o teste que o legado tinha, no mesmo cenário: um consumidor com os três cartões, duzentos reais
 * em cada, e uma compra de cem num deles. O alimentação cobra noventa, o combustível cento e trinta
 * e cinco e a farmácia o valor integral. Os outros dois cartões não se mexem, e o extrato guarda
 * uma linha só, com o valor informado e o cobrado.
 *
 * <p>
 * As partes estão provadas em separado: a regra no caso de uso, com fakes, e o saldo e o extrato na
 * persistência. Nenhuma delas prova que, juntas, o cartão certo é o que é debitado.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CompraDePontaAPontaTeste {

    private static final String SALDO_DE_PARTIDA = "200";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @ParameterizedTest
    @CsvSource({ "ALIMENTACAO, 110, 90", "COMBUSTIVEL, 65, 135", "FARMACIA, 100, 100" })
    void deve_cobrar_o_preco_do_tipo_e_so_do_cartao_comprado_quando_comprar_cem_de_duzentos(
            TipoCartao tipo, String saldoFinal, String cobrado) throws Exception {
        // Preparar: um consumidor com os três cartões, e duzentos reais em cada um
        var numeros = new EnumMap<TipoCartao, String>(TipoCartao.class);
        for (var cartao : TipoCartao.values()) {
            numeros.put(cartao, NumerosDeCartaoDeTeste.proximo());
        }
        var localizacao = mvc
                .perform(post("/api/v1/consumidores").contentType(MediaType.APPLICATION_JSON)
                        .content(CorposDaApi.cadastroComTresCartoes(DocumentosDeTeste.proximo(),
                                numeros.get(TipoCartao.ALIMENTACAO),
                                numeros.get(TipoCartao.COMBUSTIVEL),
                                numeros.get(TipoCartao.FARMACIA))))
                .andExpect(status().isCreated()).andReturn().getResponse().getHeader("Location");
        for (var cartao : TipoCartao.values()) {
            mvc.perform(post("/api/v1/creditos").contentType(MediaType.APPLICATION_JSON)
                    .content(CorposDaApi.credito(numeros.get(cartao), cartao, "200.00")))
                    .andExpect(status().isCreated());
        }

        // Agir: uma compra de cem no cartão do tipo
        mvc.perform(post("/api/v1/compras").contentType(MediaType.APPLICATION_JSON)
                .content(CorposDaApi.compra(tipo, numeros.get(tipo), "100.00")))
                .andExpect(status().isCreated());

        // Verificar o saldo de cada cartão: o comprado muda, e os outros dois ficam em duzentos
        var esperados = new EnumMap<>(Map.of(TipoCartao.ALIMENTACAO, SALDO_DE_PARTIDA,
                TipoCartao.COMBUSTIVEL, SALDO_DE_PARTIDA, TipoCartao.FARMACIA, SALDO_DE_PARTIDA));
        esperados.put(tipo, saldoFinal);
        var consumidor = mvc.perform(get(localizacao)).andReturn().getResponse()
                .getContentAsString();
        for (var cartao : TipoCartao.values()) {
            List<String> saldos = JsonPath.read(consumidor,
                    "$.cartoes[?(@.tipo=='" + cartao.name() + "')].saldo");
            assertThat(new BigDecimal(saldos.get(0))).as("saldo do cartão " + cartao)
                    .isEqualByComparingTo(esperados.get(cartao));
        }

        // Verificar o extrato: uma linha só, do cartão comprado, com o valor informado e o cobrado
        var linhas = jdbc.queryForList("""
                select c.tipo, e.valor_informado, e.valor_cobrado
                from extratos e join cartoes c on c.id = e.cartao_id
                where c.numero in (?, ?, ?)""", numeros.get(TipoCartao.ALIMENTACAO),
                numeros.get(TipoCartao.COMBUSTIVEL), numeros.get(TipoCartao.FARMACIA));
        assertThat(linhas).hasSize(1);
        assertThat(linhas.get(0).get("TIPO")).isEqualTo(tipo.name());
        assertThat((BigDecimal) linhas.get(0).get("VALOR_INFORMADO")).isEqualByComparingTo("100");
        assertThat((BigDecimal) linhas.get(0).get("VALOR_COBRADO")).isEqualByComparingTo(cobrado);
    }
}
