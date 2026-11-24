package br.com.alelo.consumer.consumerpat.dominio.cartao;

/**
 * Papel de um dado sensível que sabe se mostrar sem se revelar.
 */
public interface Mascaravel extends ComDigitos {

    /** Quantos dígitos, contados do fim, a forma mascarada deixa à vista. */
    int DIGITOS_VISIVEIS = 4;

    /**
     * A forma que se pode imprimir: os últimos {@value #DIGITOS_VISIVEIS} dígitos, precedidos de
     * asteriscos, como {@code ****2222}.
     *
     * <p>
     * É a única forma que aparece em mensagem de erro, em resposta e em log. A máscara é aplicada
     * onde o dado nasce, e não por quem o imprime, porque quem imprime pode esquecer.
     *
     * @return o dado mascarado, que não permite reconstituir os demais dígitos
     */
    default String mascarado() {
        var inicioDaParteVisivel = digitos().length() - DIGITOS_VISIVEIS;
        return "****" + digitos().substring(inicioDaParteVisivel);
    }
}
