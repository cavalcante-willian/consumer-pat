package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao;

import br.com.alelo.consumer.consumerpat.dominio.portas.BuscadorDeCartaoParaAtualizacao;

/**
 * Operação-base dos papéis do cartão que buscam o cartão para escrever: expõe quem o busca.
 *
 * <p>
 * É compartilhada pela compra e pelo crédito, e por isso mora na raiz do pacote, e não em um dos
 * dois.
 */
public interface ComBuscadorDeCartaoParaAtualizacao {

    /**
     * Quem busca o cartão, travando-o até o fim da transação.
     *
     * @return a porta de busca
     */
    BuscadorDeCartaoParaAtualizacao buscador();
}
