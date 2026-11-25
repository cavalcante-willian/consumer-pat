package br.com.alelo.consumer.consumerpat.dominio.log;

import java.util.Optional;

/**
 * Papel de registrar uma situação inesperada, mas não crítica: um conflito, um tempo de espera por
 * trava esgotado, e tudo o que se recuperou sozinho.
 */
public interface AvisoDeLog extends RegistradorDeLog {

    /**
     * Registra o evento no nível de aviso.
     *
     * @param evento o nome estável do evento
     * @param campos os dados do evento
     */
    default void aviso(String evento, CampoDeLog... campos) {
        registrar(NivelDeLog.AVISO, evento, Optional.empty(), campos);
    }
}
