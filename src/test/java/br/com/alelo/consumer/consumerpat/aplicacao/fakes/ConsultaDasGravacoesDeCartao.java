package br.com.alelo.consumer.consumerpat.aplicacao.fakes;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;

import java.util.List;

/**
 * Papel do fake que deixa o teste ver o que foi gravado, para afirmar que um pedido recusado não
 * gravou nada. Não existe na porta real: é só para conferir.
 */
public interface ConsultaDasGravacoesDeCartao extends ComCartoesEmMemoria {

    /**
     * Os cartões gravados, em ordem, sem contar os iniciais.
     *
     * @return uma cópia da lista de gravações
     */
    default List<Cartao> gravados() {
        return List.copyOf(memoria().gravacoes());
    }
}
