package br.com.alelo.consumer.consumerpat.dominio.portas;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;

/**
 * Papel de quem sabe gravar um cartão.
 *
 * <p>
 * Grava o estado novo de um cartão que já existe, e é por isso que só faz sentido sobre um cartão
 * lido para atualização: gravar um cartão lido sem trava sobrescreveria o saldo de quem gravou
 * entre a leitura e a escrita. Não cria cartão: criar exige o consumidor dono, e virá com o
 * cadastro.
 */
public interface GravadorDeCartao {

    /**
     * Grava o novo estado do cartão que existe com o mesmo número e tipo.
     *
     * @param cartao o cartão a gravar
     * @return o cartão como ficou gravado
     * @throws IllegalStateException se não existe cartão com esse número e tipo: é bug de quem
     *                               chamou, porque o caso de uso sempre lê o cartão antes
     */
    Cartao salvar(Cartao cartao);
}
