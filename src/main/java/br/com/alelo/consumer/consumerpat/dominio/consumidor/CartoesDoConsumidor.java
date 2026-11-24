package br.com.alelo.consumer.consumerpat.dominio.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;

import java.util.List;
import java.util.Optional;

/**
 * Os cartões de um consumidor: no máximo um de cada tipo.
 *
 * <p>
 * É a invariante que o legado guardava com três pares de colunas, uma por tipo, e que aqui é uma
 * regra do tipo: uma lista com dois cartões do mesmo tipo não existe. O banco repete a regra com
 * uma restrição de unicidade, mas o domínio não espera o banco para recusar.
 *
 * @param cartoes os cartões, em lista imutável, sem tipo repetido
 */
public record CartoesDoConsumidor(List<Cartao> cartoes) {

    /**
     * Cria a coleção, com uma cópia imutável da lista.
     *
     * @param cartoes os cartões
     * @throws IllegalArgumentException se a lista é nula ou tem dois cartões do mesmo tipo
     */
    public CartoesDoConsumidor {
        cartoes = Optional.ofNullable(cartoes).map(List::copyOf).filter(
                lista -> lista.stream().map(Cartao::tipo).distinct().count() == lista.size())
                .orElseThrow(() -> new IllegalArgumentException(
                        "O consumidor não pode ter dois cartões do mesmo tipo"));
    }
}
