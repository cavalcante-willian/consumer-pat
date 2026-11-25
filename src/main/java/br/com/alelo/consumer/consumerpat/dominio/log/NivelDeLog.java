package br.com.alelo.consumer.consumerpat.dominio.log;

/**
 * Os níveis do vocabulário de log do projeto.
 *
 * <p>
 * É um vocabulário fechado e em português, sem tipo de biblioteca de log. O SLF4J não tem
 * {@code FATAL}, e por isso {@link #CRITICO} vira {@code ERROR} marcado, no adaptador.
 */
public enum NivelDeLog {

    /** Detalhe para desenvolvimento; desativado em produção. */
    DEPURACAO,

    /** Evento normal de operação, incluindo o resultado de negócio esperado. */
    INFORMACAO,

    /** Situação inesperada, mas não crítica, e a que se recuperou sozinha. */
    AVISO,

    /** Erro tratado que afeta a operação atual. */
    ERRO,

    /** Erro que afeta o sistema todo. */
    CRITICO
}
