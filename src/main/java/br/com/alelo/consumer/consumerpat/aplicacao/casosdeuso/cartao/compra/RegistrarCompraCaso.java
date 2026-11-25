package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.compra;

import br.com.alelo.consumer.consumerpat.dominio.portas.BuscadorDeCartaoParaAtualizacao;

import java.time.Clock;

/**
 * O caso de uso de registrar uma compra: só guarda as dependências e declara o papel.
 *
 * <p>
 * A operação, com o {@code @Transactional}, está em {@link RegistroDeCompra}, em cima de
 * {@link #buscador()}, {@link #gravador()} e {@link #relogio()}. A classe não é {@code final}
 * porque o Spring a estende para aplicar a transação.
 */
public class RegistrarCompraCaso implements RegistroDeCompra {

    private final BuscadorDeCartaoParaAtualizacao buscador;

    private final GravadorDaCompra gravador;

    private final Clock relogio;

    /**
     * Cria o caso de uso.
     *
     * @param buscador de onde o cartão é buscado, travado até o fim da transação
     * @param gravador quem grava o cartão debitado e o extrato
     * @param relogio  de onde vem o instante da compra
     */
    public RegistrarCompraCaso(BuscadorDeCartaoParaAtualizacao buscador, GravadorDaCompra gravador,
            Clock relogio) {
        this.buscador = buscador;
        this.gravador = gravador;
        this.relogio = relogio;
    }

    @Override
    public BuscadorDeCartaoParaAtualizacao buscador() {
        return buscador;
    }

    @Override
    public GravadorDaCompra gravador() {
        return gravador;
    }

    @Override
    public Clock relogio() {
        return relogio;
    }
}
