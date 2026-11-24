package br.com.alelo.consumer.consumerpat.aplicacao.fakes;

import br.com.alelo.consumer.consumerpat.dominio.extrato.Extrato;
import br.com.alelo.consumer.consumerpat.dominio.portas.ExtratoPorta;

/**
 * O registro do extrato em memória: acrescenta ao fim da lista, em ordem, e nunca altera nem apaga,
 * como a porta real.
 */
public interface RegistroDeExtratoEmMemoria extends ComExtratosEmMemoria, ExtratoPorta {

    @Override
    default void registrar(Extrato extrato) {
        extratosEmMemoria().add(extrato);
    }
}
