package br.com.alelo.consumer.consumerpat.infraestrutura.log;

import org.slf4j.Logger;

/**
 * Como um nível do projeto é escrito no SLF4J.
 */
@FunctionalInterface
interface EscritaNoSlf4j {

    /**
     * Escreve a mensagem no nível que esta escrita representa.
     *
     * @param logger   o logger do projeto
     * @param mensagem a linha do evento
     * @param causa    a causa, ou nulo quando não há
     */
    void escrever(Logger logger, String mensagem, Throwable causa);
}
