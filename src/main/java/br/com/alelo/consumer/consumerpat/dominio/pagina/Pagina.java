package br.com.alelo.consumer.consumerpat.dominio.pagina;

import java.util.List;
import java.util.Objects;

/**
 * Uma página de resultados, sem depender de nenhuma classe de paginação do Spring.
 *
 * <p>
 * Guarda os itens da página, qual é ela, o tamanho pedido e o total do resultado todo. A página
 * começa em 1, que é o contrato da API: a conversão para o que o banco espera, que começa em 0, é
 * do adaptador, uma vez, e não vaza para cá. Uma página além da última é uma página vazia, e não um
 * erro: o resultado existe, só aquela página não tem itens.
 *
 * @param itens         os itens desta página, em lista imutável
 * @param pagina        o número desta página, a partir de 1
 * @param tamanhoPagina o tamanho pedido, e não a quantidade de itens desta página
 * @param total         o total de itens do resultado todo
 * @param <T>           o tipo dos itens
 */
public record Pagina<T>(List<T> itens, int pagina, int tamanhoPagina, long total)
        implements ComTotalDePaginas {

    /**
     * Cria a página, com uma cópia imutável dos itens.
     *
     * @param itens         os itens
     * @param pagina        o número da página
     * @param tamanhoPagina o tamanho pedido
     * @param total         o total do resultado
     * @throws NullPointerException se faltam os itens
     */
    public Pagina {
        itens = List.copyOf(Objects.requireNonNull(itens, "A página precisa de itens"));
    }
}
