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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Dez compras e dez créditos de dez reais disputam o mesmo cartão: o crédito e o débito travam a
 * mesma linha, então um não pode apagar o outro. As compras somam no máximo o saldo inicial, e por
 * isso nenhuma pode ser recusada, em qualquer ordem em que as respostas cheguem.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ConcorrenciaDeCompraECreditoTeste {

    private static final int OPERACOES = 20;

    @Autowired
    private MockMvc mvc;

    @Test
    void deve_manter_o_saldo_inicial_quando_dez_compras_e_dez_creditos_simultaneos()
            throws Exception {
        // Preparar: dez créditos e dez compras na mesma disputa
        var api = new ChamadasDaApi(mvc);
        var cartao = api.prepararFarmaciaComSaldo("100.00");
        var creditos = IntStream.range(0, OPERACOES / 2)
                .<Callable<Integer>>mapToObj(i -> () -> api.creditar(cartao.numero(), "10.00"));
        var compras = IntStream.range(0, OPERACOES / 2)
                .<Callable<Integer>>mapToObj(i -> () -> api.comprar(cartao.numero(), "10.00"));
        var operacoes = Stream.concat(creditos, compras).toList();

        // Agir
        var status = DisputaSimultanea.disputar(operacoes);

        // Verificar: todas passam, e dez entram e dez saem
        assertThat(status).containsOnly(201);
        assertThat(Collections.frequency(status, 201)).isEqualTo(OPERACOES);
        assertThat(new BigDecimal(api.saldoDe(cartao))).isEqualByComparingTo("100.00");
    }
}
