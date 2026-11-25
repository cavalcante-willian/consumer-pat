package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.saude;

import br.com.alelo.consumer.consumerpat.dominio.saude.SaudePorta;

/**
 * Operação-base do papel de consultar a saúde: expõe quem a consulta.
 */
public interface ComConsultorDeSaude {

    /**
     * Quem diz o estado de cada dependência crítica.
     *
     * @return a porta de saúde
     */
    SaudePorta saude();
}
