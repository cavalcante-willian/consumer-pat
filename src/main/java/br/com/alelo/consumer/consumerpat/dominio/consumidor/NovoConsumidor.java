package br.com.alelo.consumer.consumerpat.dominio.consumidor;

import java.util.Objects;

/**
 * Um consumidor que ainda não foi gravado: os dados e os cartões, e nada mais.
 *
 * <p>
 * Não tem id nem versão porque quem os dá é o banco, e um tipo que os carregasse com um valor falso
 * ("zero, ainda não tem") obrigaria todo chamador a lembrar do que o valor quer dizer. A gravação
 * devolve um {@link Consumidor}, esse sim com id.
 *
 * @param dados   os dados cadastrais
 * @param cartoes os cartões, que nascem com o saldo que o chamador der
 */
public record NovoConsumidor(DadosDoConsumidor dados, CartoesDoConsumidor cartoes) {

    /**
     * Cria o consumidor a gravar.
     *
     * @param dados   os dados cadastrais
     * @param cartoes os cartões
     * @throws NullPointerException se faltam os dados ou os cartões
     */
    public NovoConsumidor {
        Objects.requireNonNull(dados, "O novo consumidor precisa de dados");
        Objects.requireNonNull(cartoes, "O novo consumidor precisa da coleção de cartões");
    }
}
