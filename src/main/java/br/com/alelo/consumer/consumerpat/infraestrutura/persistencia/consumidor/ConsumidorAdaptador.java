package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.portas.ConsumidorPorta;

/**
 * Implementa a porta do consumidor sobre o banco.
 *
 * <p>
 * É o único tipo público do consumidor: só guarda os repositórios e declara os papéis, e cada
 * operação mora no seu arquivo. O construtor é pacote-privado, e o Spring o chama mesmo assim,
 * porque a composição importa a classe, e a API não consegue nem escrever o tipo dos repositórios.
 */
public class ConsumidorAdaptador implements ConsumidorPorta, BuscaDeConsumidorNoBanco,
        CriacaoDeConsumidorNoBanco, AtualizacaoDeConsumidorNoBanco {

    private final ConsumidorRepositorioJpa consumidores;

    private final CartaoRepositorioJpa cartoes;

    ConsumidorAdaptador(ConsumidorRepositorioJpa consumidores, CartaoRepositorioJpa cartoes) {
        this.consumidores = consumidores;
        this.cartoes = cartoes;
    }

    @Override
    public ConsumidorRepositorioJpa repositorioDeConsumidores() {
        return consumidores;
    }

    @Override
    public CartaoRepositorioJpa repositorioDeCartoes() {
        return cartoes;
    }
}
