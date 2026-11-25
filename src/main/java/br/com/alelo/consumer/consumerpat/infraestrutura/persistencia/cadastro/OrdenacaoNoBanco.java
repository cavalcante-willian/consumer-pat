package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cadastro;

import br.com.alelo.consumer.consumerpat.dominio.pagina.DirecaoDeOrdenacao;
import br.com.alelo.consumer.consumerpat.dominio.pagina.OrdenacaoDeConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.pagina.PedidoDePagina;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Traduz o pedido de página do domínio no que o banco espera.
 *
 * <p>
 * É onde mora o que o domínio não sabe: o que cada ordenação significa na entidade, e que o banco
 * conta as páginas a partir de 0 enquanto o contrato as conta a partir de 1. A conversão da página
 * acontece aqui, uma vez, e não vaza para o domínio. Toda ordenação desempata por id, na mesma
 * direção que o contrato pediu para o campo, para dois consumidores com o mesmo nome não trocarem
 * de lugar entre duas consultas.
 */
final class OrdenacaoNoBanco {

    private static final Map<OrdenacaoDeConsumidor, String> PROPRIEDADES = new EnumMap<>(
            Map.of(OrdenacaoDeConsumidor.ID, "id", OrdenacaoDeConsumidor.NOME, "nome"));

    private static final Map<DirecaoDeOrdenacao, Sort.Direction> SENTIDOS = new EnumMap<>(
            Map.of(DirecaoDeOrdenacao.ASC, Sort.Direction.ASC, DirecaoDeOrdenacao.DESC,
                    Sort.Direction.DESC));

    private OrdenacaoNoBanco() {
    }

    /**
     * Monta a página para o Spring Data.
     *
     * @param pedido o pedido do domínio, com a página a partir de 1
     * @return a página a partir de 0, com a ordenação e o desempate por id
     */
    static Pageable de(PedidoDePagina pedido) {
        var sentido = SENTIDOS.get(pedido.direcao());
        var ordens = Stream
                .of(new Sort.Order(sentido, PROPRIEDADES.get(pedido.ordenacao())),
                        new Sort.Order(sentido, "id"))
                .collect(Collectors.toMap(Sort.Order::getProperty, ordem -> ordem,
                        (primeira, repetida) -> primeira, LinkedHashMap::new))
                .values().stream().toList();
        return PageRequest.of(pedido.pagina() - 1, pedido.tamanhoPagina(), Sort.by(ordens));
    }
}
