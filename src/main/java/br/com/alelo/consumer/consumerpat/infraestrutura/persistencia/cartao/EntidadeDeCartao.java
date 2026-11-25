package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ConsumidorEntidade;

/**
 * Converte um cartão do domínio na linha a gravar, com o dono.
 *
 * <p>
 * É para o cartão que nasce junto do consumidor: o saldo atualizado de um cartão que já existe vai
 * por {@code AtualizadorDeEntidadeDeCartao}, e o dono nunca muda.
 */
public final class EntidadeDeCartao {

    private EntidadeDeCartao() {
    }

    /**
     * Cria a linha do cartão.
     *
     * @param cartao o cartão do domínio
     * @param dono   a linha do consumidor a que o cartão pertence, já gravada
     * @return a linha, ainda não gravada
     */
    public static CartaoEntidade criar(Cartao cartao, ConsumidorEntidade dono) {
        var entidade = new CartaoEntidade();
        entidade.setConsumidor(dono);
        entidade.setTipo(cartao.tipo());
        entidade.setNumero(cartao.numero().digitos());
        entidade.setSaldo(cartao.saldo().valor());
        return entidade;
    }
}
