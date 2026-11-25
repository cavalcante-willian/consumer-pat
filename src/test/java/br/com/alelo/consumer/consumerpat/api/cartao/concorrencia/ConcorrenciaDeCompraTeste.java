package br.com.alelo.consumer.consumerpat.api.cartao.concorrencia;

import br.com.alelo.consumer.consumerpat.fabricas.DisputaSimultanea;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Vinte compras de dez reais disputam um cartão de cem: a garantia de que cada compra confirmada
 * desconta o saldo, e de que nenhuma passa do saldo, é do banco, pelo lock do cartão. O teste
 * confere a invariante, e não a ordem em que as respostas chegam.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ConcorrenciaDeCompraTeste {

    private static final int COMPRAS = 20;

    @Autowired
    private MockMvc mvc;

    @Test
    void deve_manter_o_saldo_exato_quando_vinte_compras_simultaneas_no_mesmo_cartao()
            throws Exception {
        // Preparar: o saldo cobre exatamente dez das vinte compras
        var api = new ChamadasDaApi(mvc);
        var cartao = api.prepararFarmaciaComSaldo("100.00");
        var compras = IntStream.range(0, COMPRAS)
                .<Callable<Integer>>mapToObj(i -> () -> api.comprar(cartao.numero(), "10.00"))
                .toList();

        // Agir: todas largam juntas
        var status = DisputaSimultanea.disputar(compras);

        // Verificar: só compra aceita ou recusada por saldo; exatamente dez passam, dez ficam sem
        // saldo, e o saldo zera
        assertThat(status).containsOnly(201, 422);
        assertThat(Collections.frequency(status, 201)).isEqualTo(10);
        assertThat(Collections.frequency(status, 422)).isEqualTo(10);
        assertThat(new BigDecimal(api.saldoDe(cartao))).isEqualByComparingTo("0.00");
    }
}
