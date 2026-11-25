package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cadastro;

import br.com.alelo.consumer.consumerpat.dominio.portas.ConsumidorPorta;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao.CartaoRepositorioJpa;
// editorconfig-checker-disable-next-line
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ConsumidorRepositorioJpa;

/**
 * Implementa a porta do consumidor sobre o banco.
 *
 * <p>
 * É o único tipo público do consumidor: só guarda os repositórios e declara os papéis, e cada
 * operação mora no seu arquivo. O construtor é pacote-privado, e o Spring o chama mesmo assim,
 * porque a composição importa a classe, e a API não deve nem conhecer o tipo dos repositórios.
 */
public class ConsumidorAdaptador implements ConsumidorPorta, BuscaDeConsumidorNoBanco,
        ListagemDeConsumidoresNoBanco, CriacaoDeConsumidorNoBanco, AtualizacaoDeConsumidorNoBanco {

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
