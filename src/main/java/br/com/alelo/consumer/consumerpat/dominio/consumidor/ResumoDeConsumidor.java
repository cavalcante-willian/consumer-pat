package br.com.alelo.consumer.consumerpat.dominio.consumidor;

import java.util.Objects;

/**
 * O que a listagem de consumidores mostra de cada um: o id e o nome.
 *
 * <p>
 * É uma projeção, e não o {@link Consumidor} inteiro, porque a listagem não precisa dos cartões nem
 * do endereço, e carregá-los por consumidor é o que fazia o legado devolver tudo de todos de uma
 * vez.
 *
 * @param id   o identificador
 * @param nome o nome
 */
public record ResumoDeConsumidor(ConsumidorId id, String nome) {

    /**
     * Cria o resumo.
     *
     * @param id   o identificador
     * @param nome o nome
     * @throws NullPointerException se falta o id ou o nome
     */
    public ResumoDeConsumidor {
        Objects.requireNonNull(id, "O resumo precisa de um id");
        Objects.requireNonNull(nome, "O resumo precisa de um nome");
    }
}
