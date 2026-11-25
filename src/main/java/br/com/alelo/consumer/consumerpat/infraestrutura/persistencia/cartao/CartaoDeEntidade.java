package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;

/**
 * Converte a linha do banco no cartão do domínio.
 *
 * <p>
 * É a única peça que sabe como uma linha de {@code cartoes} vira um {@link Cartao}. Não lê os
 * campos de auditoria nem a versão: o domínio não os conhece. Uma linha que o domínio recusaria,
 * como um número fora do formato ou um saldo fora da escala, é bug de quem gravou, e lança.
 */
public final class CartaoDeEntidade {

    private CartaoDeEntidade() {
    }

    /**
     * Converte a linha no cartão.
     *
     * @param entidade a linha lida do banco
     * @return o cartão com o número, o tipo e o saldo da linha
     */
    public static Cartao converter(CartaoEntidade entidade) {
        return new Cartao(new NumeroDeCartao(entidade.getNumero()), entidade.getTipo(),
                new Dinheiro(entidade.getSaldo()));
    }
}
