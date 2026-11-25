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
 * Vinte créditos de dez reais disputam o mesmo cartão: nenhum pode se perder, e o saldo final é a
 * soma de todos. É o defeito do legado de ler o saldo, somar em memória e gravar, visto do lado do
 * crédito.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ConcorrenciaDeCreditoTeste {

    private static final int CREDITOS = 20;

    @Autowired
    private MockMvc mvc;

    @Test
    void deve_somar_todos_os_creditos_quando_vinte_creditos_simultaneos_no_mesmo_cartao()
            throws Exception {
        // Preparar
        var api = new ChamadasDaApi(mvc);
        var cartao = api.prepararFarmaciaComSaldo("100.00");
        var creditos = IntStream.range(0, CREDITOS)
                .<Callable<Integer>>mapToObj(i -> () -> api.creditar(cartao.numero(), "10.00"))
                .toList();

        // Agir
        var status = DisputaSimultanea.disputar(creditos);

        // Verificar: todos entram, e o saldo é os cem iniciais mais os duzentos creditados
        assertThat(status).containsOnly(201);
        assertThat(Collections.frequency(status, 201)).isEqualTo(CREDITOS);
        assertThat(new BigDecimal(api.saldoDe(cartao))).isEqualByComparingTo("300.00");
    }
}
