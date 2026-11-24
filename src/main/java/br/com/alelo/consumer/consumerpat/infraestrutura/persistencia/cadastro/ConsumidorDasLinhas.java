package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cadastro;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao.CartaoDeEntidade;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao.CartaoEntidade;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ConsumidorDeEntidade; // editorconfig-checker-disable-line
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ConsumidorEntidade;

import java.util.List;

/**
 * Monta o consumidor do domínio a partir das linhas do consumidor e dos cartões dele.
 *
 * <p>
 * É a única peça que conhece as duas tabelas ao mesmo tempo: o pacote do consumidor não conhece o
 * cartão, e o do cartão não converte o consumidor. Fica aqui, no pacote que as compõe.
 */
final class ConsumidorDasLinhas {

    private ConsumidorDasLinhas() {
    }

    /**
     * Converte as linhas.
     *
     * @param entidade a linha do consumidor
     * @param cartoes  as linhas dos cartões dele
     * @return o consumidor do domínio, com id, versão e cartões
     */
    static Consumidor converter(ConsumidorEntidade entidade, List<CartaoEntidade> cartoes) {
        return ConsumidorDeEntidade.converter(entidade,
                cartoes.stream().map(CartaoDeEntidade::converter).toList());
    }
}
