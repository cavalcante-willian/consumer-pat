package br.com.alelo.consumer.consumerpat.dominio.pagina;

/**
 * Papel de uma página que sabe quantas páginas o resultado todo tem.
 *
 * <p>
 * É a conta do teto da divisão: 50 mil itens em páginas de 20 são 2.500 páginas, e 50.001 são
 * 2.501. Um resultado vazio tem zero páginas.
 */
public interface ComTotalDePaginas {

    /**
     * O total de itens do resultado, somando todas as páginas.
     *
     * @return o total
     */
    long total();

    /**
     * O tamanho pedido para a página.
     *
     * @return o tamanho
     */
    int tamanhoPagina();

    /**
     * Quantas páginas o resultado tem.
     *
     * @return o total de itens dividido pelo tamanho, arredondado para cima
     */
    default long totalPaginas() {
        return (total() + tamanhoPagina() - 1) / tamanhoPagina();
    }
}
