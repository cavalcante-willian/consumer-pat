package br.com.alelo.consumer.consumerpat.dominio.log;

import java.util.Optional;

/**
 * Papel de registrar um evento de depuração: detalhe para desenvolvimento, desligado em produção.
 */
public interface DepuracaoDeLog extends RegistradorDeLog {

    /**
     * Registra o evento no nível de depuração.
     *
     * @param evento o nome estável do evento
     * @param campos os dados do evento
     */
    default void depuracao(String evento, CampoDeLog... campos) {
        registrar(NivelDeLog.DEPURACAO, evento, Optional.empty(), campos);
    }
}
