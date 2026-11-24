package br.com.alelo.consumer.consumerpat.dominio.cartao;

/**
 * Papel de um tipo de cartão que corresponde a um tipo de estabelecimento.
 */
public interface ComCodigoDeEstabelecimento {

    /**
     * O código do tipo de estabelecimento em que este tipo de cartão pode ser usado: 1 para
     * alimentação, 2 para farmácia e 3 para posto de combustível.
     *
     * @return o código do tipo de estabelecimento
     */
    int codigoDeEstabelecimento();
}
