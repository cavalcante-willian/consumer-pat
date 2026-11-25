package br.com.alelo.consumer.consumerpat.dominio.log;

import java.util.Optional;

/**
 * Papel de registrar um erro com a causa, e só para a causa que não carrega dado sensível: a pilha
 * de uma exceção pode trazer o valor na mensagem, e nada no log a mascara.
 */
public interface ErroComCausaDeLog extends RegistradorDeLog {

    /**
     * Registra o evento no nível de erro, com a causa.
     *
     * @param evento o nome estável do evento
     * @param causa  a causa, que não deve carregar dado sensível
     * @param campos os dados do evento
     */
    default void erroComCausa(String evento, Throwable causa, CampoDeLog... campos) {
        registrar(NivelDeLog.ERRO, evento, Optional.of(causa), campos);
    }
}
