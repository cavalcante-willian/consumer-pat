package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.credito;

import br.com.alelo.consumer.consumerpat.dominio.log.SaidaDeLogPorta;
import br.com.alelo.consumer.consumerpat.dominio.portas.BuscadorDeCartaoParaAtualizacao;
import br.com.alelo.consumer.consumerpat.dominio.portas.GravadorDeCartao;

/**
 * O caso de uso de creditar um cartão: só guarda as portas e declara o papel.
 *
 * <p>
 * A operação, com o {@code @Transactional}, está em {@link CreditoDeCartao}, em cima de
 * {@link #buscador()}, {@link #gravador()} e {@link #saida()}. A classe não é {@code final} porque
 * o Spring a estende para aplicar a transação.
 */
public class CreditarCartaoCaso implements CreditoDeCartao {

    private final BuscadorDeCartaoParaAtualizacao buscador;

    private final GravadorDeCartao gravador;

    private final SaidaDeLogPorta saida;

    /**
     * Cria o caso de uso.
     *
     * @param buscador de onde o cartão é buscado, travado até o fim da transação
     * @param gravador onde o cartão com o novo saldo é gravado
     * @param saida    a porta de log
     */
    public CreditarCartaoCaso(BuscadorDeCartaoParaAtualizacao buscador, GravadorDeCartao gravador,
            SaidaDeLogPorta saida) {
        this.buscador = buscador;
        this.gravador = gravador;
        this.saida = saida;
    }

    @Override
    public BuscadorDeCartaoParaAtualizacao buscador() {
        return buscador;
    }

    @Override
    public GravadorDeCartao gravador() {
        return gravador;
    }

    @Override
    public SaidaDeLogPorta saida() {
        return saida;
    }
}
