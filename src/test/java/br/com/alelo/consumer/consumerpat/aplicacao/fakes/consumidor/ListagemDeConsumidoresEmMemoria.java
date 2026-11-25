package br.com.alelo.consumer.consumerpat.aplicacao.fakes.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ResumoDeConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.pagina.DirecaoDeOrdenacao;
import br.com.alelo.consumer.consumerpat.dominio.pagina.OrdenacaoDeConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.pagina.Pagina;
import br.com.alelo.consumer.consumerpat.dominio.pagina.PedidoDePagina;
import br.com.alelo.consumer.consumerpat.dominio.portas.ListadorDeConsumidores;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Lista os consumidores em memória, com a regra do banco: ordenação fechada, desempate por id na
 * mesma direção, e o corte da página a partir de 1.
 */
public interface ListagemDeConsumidoresEmMemoria
        extends ComConsumidoresEmMemoria, ListadorDeConsumidores {

    /** O campo de cada ordenação; o desempate por id vem depois. */
    Map<OrdenacaoDeConsumidor, Comparator<Consumidor>> CAMPOS = new EnumMap<>(
            Map.of(OrdenacaoDeConsumidor.ID,
                    Comparator.comparingLong((Consumidor consumidor) -> consumidor.id().valor()),
                    OrdenacaoDeConsumidor.NOME,
                    Comparator.comparing((Consumidor consumidor) -> consumidor.dados().nome())));

    /** O que cada direção faz com o comparador. */
    Map<DirecaoDeOrdenacao, UnaryOperator<Comparator<Consumidor>>> SENTIDOS = new EnumMap<>(
            Map.of(DirecaoDeOrdenacao.ASC, comparador -> comparador, DirecaoDeOrdenacao.DESC,
                    Comparator::reversed));

    @Override
    default Pagina<ResumoDeConsumidor> listar(PedidoDePagina pedido) {
        var ordem = SENTIDOS.get(pedido.direcao()).apply(CAMPOS.get(pedido.ordenacao())
                .thenComparingLong(consumidor -> consumidor.id().valor()));
        var todos = memoria().consumidores().values().stream().sorted(ordem).toList();
        var itens = todos.stream().skip((long) (pedido.pagina() - 1) * pedido.tamanhoPagina())
                .limit(pedido.tamanhoPagina())
                .map(consumidor -> new ResumoDeConsumidor(consumidor.id(),
                        consumidor.dados().nome()))
                .toList();
        return new Pagina<>(itens, pedido.pagina(), pedido.tamanhoPagina(), todos.size());
    }
}
