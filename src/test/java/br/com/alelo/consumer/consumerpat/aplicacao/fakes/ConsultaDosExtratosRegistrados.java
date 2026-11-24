package br.com.alelo.consumer.consumerpat.aplicacao.fakes;

import br.com.alelo.consumer.consumerpat.dominio.extrato.Extrato;

import java.util.List;

/**
 * Papel do fake que deixa o teste ver o que foi registrado. Não existe na porta real, que só
 * registra: é só para conferir.
 */
public interface ConsultaDosExtratosRegistrados extends ComExtratosEmMemoria {

    /**
     * Os extratos registrados, em ordem.
     *
     * @return uma cópia da lista
     */
    default List<Extrato> registrados() {
        return List.copyOf(extratosEmMemoria());
    }
}
