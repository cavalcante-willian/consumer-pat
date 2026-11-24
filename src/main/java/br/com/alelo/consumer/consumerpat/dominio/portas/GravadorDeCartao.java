package br.com.alelo.consumer.consumerpat.dominio.portas;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;

/**
 * Papel de quem sabe gravar um cartão.
 *
 * <p>
 * Grava o estado atual do cartão, e é por isso que só faz sentido sobre um cartão lido para
 * atualização: gravar um cartão lido sem trava sobrescreveria o saldo de quem gravou entre a
 * leitura e a escrita.
 */
public interface GravadorDeCartao {

    /**
     * Grava o cartão, criando-o ou substituindo o que existe com o mesmo número e tipo.
     *
     * @param cartao o cartão a gravar
     * @return o cartão como ficou gravado
     */
    Cartao salvar(Cartao cartao);
}
