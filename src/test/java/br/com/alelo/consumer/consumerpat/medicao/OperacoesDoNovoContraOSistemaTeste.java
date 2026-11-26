package br.com.alelo.consumer.consumerpat.medicao;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Percorre todas as operações do driver contra o sistema novo de verdade, num servidor real em
 * porta aleatória, com poucos consumidores. A medição só roda sob demanda e leva minutos, e sem
 * este teste uma rota que mudasse só seria notada na hora de medir.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OperacoesDoNovoContraOSistemaTeste {

    private static final int CONSUMIDORES = 3;

    @LocalServerPort
    private int porta;

    private static List<Integer> statusDe(List<Pedido> pedidos) throws Exception {
        var status = new ArrayList<Integer>();
        for (var pedido : pedidos) {
            status.add(pedido.chamada().call().status());
        }
        return status;
    }

    @Test
    void deve_responder_a_toda_operacao_do_driver_quando_o_sistema_novo_esta_de_pe()
            throws Exception {
        // Preparar
        var operacoes = new OperacoesDoNovo(new ClienteHttp("http://localhost:" + porta));
        var cadastros = new ArrayList<Integer>();
        for (int indice = 0; indice < CONSUMIDORES; indice++) {
            cadastros.add(operacoes.cadastrar(indice).status());
        }

        // Verificar: cadastro, listagens, consulta, crédito e compra, tudo respondido de verdade
        assertThat(cadastros).containsOnly(201);
        assertThat(statusDe(operacoes.listagens(CONSUMIDORES))).containsOnly(200);
        assertThat(statusDe(operacoes.consultas(CONSUMIDORES))).containsOnly(200);
        assertThat(statusDe(operacoes.creditos(CONSUMIDORES))).hasSize(9).containsOnly(201);
        assertThat(statusDe(operacoes.compras(CONSUMIDORES))).hasSize(9).containsOnly(201);
    }

    @Test
    void deve_debitar_o_saldo_exato_quando_preparar_comprar_e_ler_pela_disputa() throws Exception {
        // Preparar
        var operacoes = new OperacoesDoNovo(new ClienteHttp("http://localhost:" + porta));
        var cartao = operacoes.prepararFarmaciaComSaldo("100.00");

        // Agir
        var status = operacoes.comprarNaFarmacia(cartao, "10.00");

        // Verificar
        assertThat(status).isEqualTo(201);
        assertThat(operacoes.saldoDe(cartao)).isEqualByComparingTo("90.00");
    }
}
