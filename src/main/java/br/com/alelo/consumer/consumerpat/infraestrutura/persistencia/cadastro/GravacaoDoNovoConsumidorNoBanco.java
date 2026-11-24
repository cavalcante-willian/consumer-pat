package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cadastro;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.NovoConsumidor;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao.ComRepositorioDeCartoes;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao.EntidadeDeCartao;
// editorconfig-checker-disable-next-line
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.AtualizadorDeEntidadeDeConsumidor;
// editorconfig-checker-disable-next-line
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ComRepositorioDeConsumidores;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ConsumidorEntidade;

/**
 * Grava o consumidor novo e os cartões dele, na transação do chamador.
 *
 * <p>
 * Grava o consumidor primeiro, porque o cartão aponta para ele, e depois os cartões, e descarrega
 * as duas escritas: o id, a versão e a auditoria só existem depois delas, e o consumidor devolvido
 * é o que ficou gravado, e não o que foi pedido.
 */
interface GravacaoDoNovoConsumidorNoBanco
        extends ComRepositorioDeConsumidores, ComRepositorioDeCartoes {

    /**
     * Grava o consumidor e os cartões.
     *
     * @param novo o consumidor a gravar, já sem conflito
     * @return o consumidor gravado, com id e versão
     */
    default Consumidor gravar(NovoConsumidor novo) {
        var dono = repositorioDeConsumidores().saveAndFlush(AtualizadorDeEntidadeDeConsumidor
                .atualizar(new ConsumidorEntidade(), novo.dados()));
        var cartoes = repositorioDeCartoes().saveAll(novo.cartoes().cartoes().stream()
                .map(cartao -> EntidadeDeCartao.criar(cartao, dono)).toList());
        repositorioDeCartoes().flush();
        return ConsumidorDasLinhas.converter(dono, cartoes);
    }
}
