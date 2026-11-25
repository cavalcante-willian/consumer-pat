package br.com.alelo.consumer.consumerpat.dominio.saude;

/**
 * A situação geral do sistema, a que o balanceador e o orquestrador leem.
 *
 * <p>
 * Saudável é funcionar, e não existir: um processo vivo com o banco inacessível não está saudável.
 */
public enum SituacaoDeSaude {

    /** Todas as dependências críticas estão disponíveis. */
    SAUDAVEL,

    /** Alguma dependência crítica está indisponível. */
    DEGRADADO
}
