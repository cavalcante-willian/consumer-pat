package br.com.alelo.consumer.consumerpat.dominio.pagina;

import java.util.Objects;
import java.util.Optional;

/**
 * O pedido de uma página da listagem de consumidores.
 *
 * <p>
 * É o que a API entrega depois de ler os parâmetros: uma página a partir de 1, um tamanho de 1 até
 * o teto, e uma ordenação. Não há pedido sem ordenação, porque paginar sem ordem determinística
 * entrega uma página incorreta com sucesso.
 *
 * @param pagina        a página pedida, a partir de 1
 * @param tamanhoPagina quantos itens por página, de 1 a {@value #TAMANHO_MAXIMO}
 * @param ordenacao     por que campo ordenar
 * @param direcao       em que sentido
 */
public record PedidoDePagina(int pagina, int tamanhoPagina, OrdenacaoDeConsumidor ordenacao,
        DirecaoDeOrdenacao direcao) {

    /** O tamanho de página quando o cliente não pede um. */
    public static final int TAMANHO_PADRAO = 20;

    /** O maior tamanho de página aceito: acima dele a resposta é 400. */
    public static final int TAMANHO_MAXIMO = 100;

    /**
     * Cria o pedido.
     *
     * @param pagina        a página, a partir de 1
     * @param tamanhoPagina o tamanho, de 1 ao teto
     * @param ordenacao     o campo
     * @param direcao       o sentido
     * @throws IllegalArgumentException se a página ou o tamanho estão fora dos limites
     * @throws NullPointerException     se falta a ordenação ou a direção
     */
    public PedidoDePagina {
        pagina = Optional.of(pagina).filter(candidata -> candidata >= 1)
                .orElseThrow(() -> new IllegalArgumentException("A página começa em 1"));
        tamanhoPagina = Optional.of(tamanhoPagina)
                .filter(candidato -> candidato >= 1 && candidato <= TAMANHO_MAXIMO)
                .orElseThrow(() -> new IllegalArgumentException(
                        "O tamanho da página vai de 1 a " + TAMANHO_MAXIMO));
        Objects.requireNonNull(ordenacao, "O pedido precisa de uma ordenação");
        Objects.requireNonNull(direcao, "O pedido precisa de uma direção");
    }
}
