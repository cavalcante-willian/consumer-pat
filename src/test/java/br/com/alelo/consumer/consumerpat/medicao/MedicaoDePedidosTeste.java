package br.com.alelo.consumer.consumerpat.medicao;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class MedicaoDePedidosTeste {

    @Test
    void deve_aquecer_e_medir_treze_execucoes_por_pedido_quando_medir() throws Exception {
        // Preparar
        var chamadas = new AtomicInteger();
        var pedido = new Pedido("chave", "descrição", () -> {
            chamadas.incrementAndGet();
            return AlvoFalso.resposta(200, 10);
        });

        // Agir
        MedicaoDePedidos.medir(List.of(pedido));

        // Verificar: três de aquecimento, que não contam, e dez medidas
        assertThat(chamadas.get()).isEqualTo(13);
    }

    @Test
    void deve_levar_a_chave_a_descricao_e_a_mediana_quando_medir() throws Exception {
        // Preparar
        var pedido = new Pedido("compra-farmacia-meio", "compra na farmácia",
                () -> AlvoFalso.resposta(201, 10));

        // Agir
        var medida = MedicaoDePedidos.medir(List.of(pedido)).get(0);

        // Verificar
        assertThat(medida.chave()).isEqualTo("compra-farmacia-meio");
        assertThat(medida.descricao()).isEqualTo("compra na farmácia");
        assertThat(medida.principal()).isPresent();
    }

    @Test
    void deve_dizer_o_tamanho_da_resposta_e_o_status_quando_medir() throws Exception {
        // Preparar
        var pedido = new Pedido("chave", "descrição", () -> AlvoFalso.resposta(200, 1_234));

        // Agir
        var resumo = MedicaoDePedidos.medir(List.of(pedido)).get(0).resumo();

        // Verificar: o tempo sozinho enganaria, e o tamanho sai com milhar
        assertThat(resumo).contains("resposta 1.234 bytes").contains("status {200=10}")
                .contains("mediana");
    }

    @Test
    void deve_contar_cada_status_quando_as_respostas_diferem() throws Exception {
        // Preparar: alterna 404 e 200 a cada chamada
        var chamadas = new AtomicInteger();
        var status = new int[] { 404, 200 };
        var pedido = new Pedido("chave", "descrição",
                () -> AlvoFalso.resposta(status[chamadas.incrementAndGet() % 2], 0));

        // Agir
        var resumo = MedicaoDePedidos.medir(List.of(pedido)).get(0).resumo();

        // Verificar: uma resposta rápida de erro não pode passar por sucesso
        assertThat(resumo).contains("status {200=5, 404=5}");
    }

    @Test
    void deve_devolver_uma_medida_por_pedido_na_ordem_quando_medir_varios() throws Exception {
        // Preparar
        var primeiro = new Pedido("a", "primeiro", () -> AlvoFalso.resposta(200, 0));
        var segundo = new Pedido("b", "segundo", () -> AlvoFalso.resposta(200, 0));

        // Agir
        var medidas = MedicaoDePedidos.medir(List.of(primeiro, segundo));

        // Verificar
        assertThat(medidas).extracting(Medida::chave).containsExactly("a", "b");
    }

    @Test
    void deve_devolver_nada_quando_nao_ha_pedidos() throws Exception {
        // Agir e Verificar
        assertThat(MedicaoDePedidos.medir(List.of())).isEmpty();
    }
}
