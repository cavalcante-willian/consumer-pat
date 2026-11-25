package br.com.alelo.consumer.consumerpat.dominio.log;

import java.util.Optional;

/**
 * Papel de registrar um erro tratado que afeta a operação atual, sem a causa.
 */
public interface ErroDeLog extends RegistradorDeLog {

    /**
     * Registra o evento no nível de erro.
     *
     * @param evento o nome estável do evento
     * @param campos os dados do evento
     */
    default void erro(String evento, CampoDeLog... campos) {
        registrar(NivelDeLog.ERRO, evento, Optional.empty(), campos);
    }
}
