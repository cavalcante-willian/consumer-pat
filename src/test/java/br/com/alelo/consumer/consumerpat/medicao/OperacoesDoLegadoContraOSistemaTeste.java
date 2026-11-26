package br.com.alelo.consumer.consumerpat.medicao;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Percorre todas as operações do driver contra o legado que ainda está nesta árvore, num servidor
 * real em porta aleatória, com poucos consumidores.
 *
 * <p>
 * Sai junto com o legado. O que fica depois dele são as {@link OperacoesDoLegado}, que falam por
 * HTTP com o original extraído do histórico, e que este teste enquanto isso mantém honestas.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OperacoesDoLegadoContraOSistemaTeste {

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
    void deve_responder_a_toda_operacao_do_driver_quando_o_legado_esta_de_pe() throws Exception {
        // Preparar: o cadastro e o crédito do legado gravam e respondem 404, porque o método é void
        // e sem corpo de resposta, e o Spring tenta resolver uma visão
        var operacoes = new OperacoesDoLegado(new ClienteHttp("http://localhost:" + porta));
        var cadastros = new ArrayList<Integer>();
        for (int indice = 0; indice < CONSUMIDORES; indice++) {
            cadastros.add(operacoes.cadastrar(indice).status());
        }

        // Verificar
        assertThat(cadastros).containsOnly(404);
        assertThat(statusDe(operacoes.listagens(CONSUMIDORES))).containsOnly(200);
        assertThat(operacoes.consultas(CONSUMIDORES)).isEmpty();
        assertThat(statusDe(operacoes.creditos(CONSUMIDORES))).hasSize(9).containsOnly(404);
        assertThat(statusDe(operacoes.compras(CONSUMIDORES))).hasSize(9).containsOnly(200);
    }

    @Test
    void deve_debitar_o_saldo_quando_preparar_comprar_e_ler_pela_disputa() throws Exception {
        // Preparar
        var operacoes = new OperacoesDoLegado(new ClienteHttp("http://localhost:" + porta));
        var cartao = operacoes.prepararFarmaciaComSaldo("100.00");

        // Agir
        var status = operacoes.comprarNaFarmacia(cartao, "10.00");

        // Verificar
        assertThat(status).isEqualTo(200);
        assertThat(operacoes.saldoDe(cartao)).isEqualByComparingTo("90.00");
    }
}
