package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao;

import br.com.alelo.consumer.consumerpat.dominio.portas.CartaoPorta;

/**
 * O adaptador da porta do cartão: implementa a busca para atualização e a gravação em cima do
 * banco.
 *
 * <p>
 * Só guarda o repositório e declara os papéis: a busca e a gravação são cada uma o seu arquivo. É o
 * único tipo público do cartão, e quem o instancia é a composition root, por importação da classe;
 * o construtor é pacote-privado porque o repositório também é, e o Spring o injeta mesmo assim.
 */
public class CartaoAdaptador implements CartaoPorta, BuscaDeCartaoNoBanco, GravacaoDeCartaoNoBanco {

    private final CartaoRepositorioJpa repositorio;

    CartaoAdaptador(CartaoRepositorioJpa repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public CartaoRepositorioJpa repositorioDeCartoes() {
        return repositorio;
    }
}
