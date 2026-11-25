package br.com.alelo.consumer.consumerpat.api.comum.tratamento;

import br.com.alelo.consumer.consumerpat.dominio.log.SaidaDeLogPorta;

/**
 * Operação-base dos papéis do tratador que registram o que traduzem: expõe a porta de log.
 */
public interface ComSaidaDeLog {

    /**
     * A porta por onde o tratador registra a falha que traduziu.
     *
     * @return a porta de log
     */
    SaidaDeLogPorta saida();
}
