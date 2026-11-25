package br.com.alelo.consumer.consumerpat.dominio.log;

import java.util.Optional;

/**
 * Papel de registrar um erro que afeta o sistema todo, como o banco indisponível.
 */
public interface CriticoDeLog extends RegistradorDeLog {

    /**
     * Registra o evento no nível crítico.
     *
     * @param evento o nome estável do evento
     * @param campos os dados do evento
     */
    default void critico(String evento, CampoDeLog... campos) {
        registrar(NivelDeLog.CRITICO, evento, Optional.empty(), campos);
    }
}
