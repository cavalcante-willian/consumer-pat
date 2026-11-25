package br.com.alelo.consumer.consumerpat.dominio.log;

import java.util.Optional;

/**
 * Papel de registrar um evento normal de operação, e também o resultado de negócio esperado, como
 * uma compra recusada por saldo insuficiente.
 */
public interface InformacaoDeLog extends RegistradorDeLog {

    /**
     * Registra o evento no nível de informação.
     *
     * @param evento o nome estável do evento
     * @param campos os dados do evento
     */
    default void informacao(String evento, CampoDeLog... campos) {
        registrar(NivelDeLog.INFORMACAO, evento, Optional.empty(), campos);
    }
}
