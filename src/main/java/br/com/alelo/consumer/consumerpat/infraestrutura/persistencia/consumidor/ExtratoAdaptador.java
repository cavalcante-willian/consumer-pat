package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

/**
 * O adaptador da porta do extrato: implementa o registro em cima do banco.
 *
 * <p>
 * Só guarda os repositórios e declara o papel. É público, e quem o instancia é a composition root,
 * por importação da classe; o construtor é pacote-privado porque os repositórios também são, e o
 * Spring os injeta mesmo assim.
 */
public class ExtratoAdaptador implements RegistroDeExtratoNoBanco {

    private final CartaoRepositorioJpa cartoes;

    private final ExtratoRepositorioJpa extratos;

    ExtratoAdaptador(CartaoRepositorioJpa cartoes, ExtratoRepositorioJpa extratos) {
        this.cartoes = cartoes;
        this.extratos = extratos;
    }

    @Override
    public CartaoRepositorioJpa repositorioDeCartoes() {
        return cartoes;
    }

    @Override
    public ExtratoRepositorioJpa repositorioDeExtratos() {
        return extratos;
    }
}
