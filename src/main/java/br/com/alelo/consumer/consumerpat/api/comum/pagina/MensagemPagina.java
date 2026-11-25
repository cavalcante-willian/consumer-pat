package br.com.alelo.consumer.consumerpat.api.comum.pagina;

import java.util.List;

/**
 * O envelope único de toda listagem paginada.
 *
 * <p>
 * Um cliente genérico de listagem sabe sempre onde estão os itens e quantas páginas há, sem tratar
 * cada endpoint à parte.
 *
 * @param itens         os itens desta página
 * @param pagina        o número desta página, a partir de 1
 * @param tamanhoPagina o tamanho pedido
 * @param total         o total de itens do resultado todo
 * @param totalPaginas  quantas páginas o resultado tem
 * @param <T>           o tipo da mensagem de cada item
 */
public record MensagemPagina<T>(List<T> itens, int pagina, int tamanhoPagina, long total,
        long totalPaginas) {
}
