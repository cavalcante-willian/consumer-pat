package br.com.alelo.consumer.consumerpat.dominio.consumidor;

import java.util.Objects;
import java.util.Optional;

/**
 * Um consumidor já gravado: o agregado raiz, com os dados cadastrais e os cartões.
 *
 * <p>
 * A versão é a da linha no banco, e quem atualiza o cadastro a reenvia: dois clientes que leram a
 * mesma versão não sobrescrevem um ao outro em silêncio, e o segundo recebe um conflito. É
 * imutável, e {@link #comDados(DadosDoConsumidor)} devolve outro consumidor com os dados trocados e
 * os mesmos cartões, o que faz da atualização de cadastro uma operação que não toca em saldo.
 *
 * @param id      o identificador
 * @param dados   os dados cadastrais
 * @param cartoes os cartões, com o saldo de cada um
 * @param versao  a versão da linha, a partir de zero
 */
public record Consumidor(ConsumidorId id, DadosDoConsumidor dados, CartoesDoConsumidor cartoes,
        long versao) {

    /**
     * Cria o consumidor.
     *
     * @param id      o identificador
     * @param dados   os dados cadastrais
     * @param cartoes os cartões
     * @param versao  a versão da linha
     * @throws NullPointerException     se falta o id, os dados ou os cartões
     * @throws IllegalArgumentException se a versão é negativa
     */
    public Consumidor {
        Objects.requireNonNull(id, "O consumidor precisa de um id");
        Objects.requireNonNull(dados, "O consumidor precisa de dados");
        Objects.requireNonNull(cartoes, "O consumidor precisa da coleção de cartões");
        versao = Optional.of(versao).filter(candidata -> candidata >= 0).orElseThrow(
                () -> new IllegalArgumentException("A versão do consumidor não pode ser negativa"));
    }

    /**
     * Troca os dados cadastrais e mantém o resto.
     *
     * @param novosDados os dados que passam a valer
     * @return outro consumidor, com o mesmo id, os mesmos cartões e a mesma versão
     */
    public Consumidor comDados(DadosDoConsumidor novosDados) {
        return new Consumidor(id, novosDados, cartoes, versao);
    }
}
