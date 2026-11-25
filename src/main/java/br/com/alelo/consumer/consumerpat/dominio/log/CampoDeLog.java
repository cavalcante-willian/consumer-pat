package br.com.alelo.consumer.consumerpat.dominio.log;

import java.util.Objects;

/**
 * Um dado do evento de log, separado da mensagem.
 *
 * <p>
 * O nome do campo descreve o dado ({@code consumidorId}, {@code numeroCartao}), e é por ele que o
 * adaptador reconhece o que deve ser mascarado: a convenção só protege o que está bem nomeado. O
 * valor pode ser nulo, e vira o texto {@code null}.
 *
 * @param nome  o nome do campo, que descreve o dado
 * @param valor o valor, de qualquer tipo
 */
public record CampoDeLog(String nome, Object valor) {

    /**
     * Cria o campo, exigindo o nome.
     *
     * @param nome  o nome do campo
     * @param valor o valor
     * @throws NullPointerException se falta o nome
     */
    public CampoDeLog {
        Objects.requireNonNull(nome, "O campo de log precisa de um nome");
    }

    /**
     * Atalho para escrever o campo na chamada do log.
     *
     * @param nome  o nome do campo
     * @param valor o valor
     * @return o campo
     */
    public static CampoDeLog campo(String nome, Object valor) {
        return new CampoDeLog(nome, valor);
    }
}
