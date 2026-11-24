package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;

/**
 * Leva o estado novo do cartão para a linha que já existe no banco.
 *
 * <p>
 * Só o saldo muda: o número e o tipo identificam a linha, e a versão e a auditoria são do
 * mecanismo, e não de quem atualiza. É por isso que o cartão não é criado por aqui: criar exige o
 * consumidor dono, que o domínio ainda não tem.
 */
final class AtualizadorDeEntidadeDeCartao {

    private AtualizadorDeEntidadeDeCartao() {
    }

    /**
     * Atualiza a linha com o saldo do cartão.
     *
     * @param entidade a linha existente, lida com trava
     * @param cartao   o cartão com o novo saldo
     * @return a mesma linha, já atualizada
     */
    static CartaoEntidade atualizar(CartaoEntidade entidade, Cartao cartao) {
        entidade.setSaldo(cartao.saldo().valor());
        return entidade;
    }
}
