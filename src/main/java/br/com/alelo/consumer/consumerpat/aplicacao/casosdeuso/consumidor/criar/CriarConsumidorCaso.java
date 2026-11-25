package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.criar;

import br.com.alelo.consumer.consumerpat.dominio.log.SaidaDeLogPorta;
import br.com.alelo.consumer.consumerpat.dominio.portas.GravadorDeNovoConsumidor;

/**
 * O caso de uso de cadastrar um consumidor: só guarda as portas e declara o papel.
 *
 * <p>
 * A operação está em {@link CadastroDeConsumidor}, em cima de {@link #gravador()} e
 * {@link #saida()}. A classe não é {@code final} porque o Spring cria um proxy que a estende para
 * aplicar a transação.
 */
public class CriarConsumidorCaso implements CadastroDeConsumidor {

    private final GravadorDeNovoConsumidor gravador;

    private final SaidaDeLogPorta saida;

    /**
     * Cria o caso de uso.
     *
     * @param gravador quem grava o consumidor novo
     * @param saida    a porta de log
     */
    public CriarConsumidorCaso(GravadorDeNovoConsumidor gravador, SaidaDeLogPorta saida) {
        this.gravador = gravador;
        this.saida = saida;
    }

    @Override
    public GravadorDeNovoConsumidor gravador() {
        return gravador;
    }

    @Override
    public SaidaDeLogPorta saida() {
        return saida;
    }
}
