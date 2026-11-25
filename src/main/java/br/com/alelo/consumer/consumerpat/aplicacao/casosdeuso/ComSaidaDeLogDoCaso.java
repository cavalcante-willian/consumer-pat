package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso;

import br.com.alelo.consumer.consumerpat.dominio.log.SaidaDeLogPorta;

/**
 * Operação-base dos papéis dos casos de uso que registram o que fizeram: expõe a porta de log.
 *
 * <p>
 * Os casos que não emitem evento, como a leitura e a listagem, não a usam e não recebem a porta.
 */
public interface ComSaidaDeLogDoCaso {

    /**
     * A porta por onde o caso de uso registra o fato.
     *
     * @return a porta de log
     */
    SaidaDeLogPorta saida();
}
