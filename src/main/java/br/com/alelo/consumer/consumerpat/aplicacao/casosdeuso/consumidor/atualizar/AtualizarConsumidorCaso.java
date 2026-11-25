package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.atualizar;

import br.com.alelo.consumer.consumerpat.dominio.log.SaidaDeLogPorta;
import br.com.alelo.consumer.consumerpat.dominio.portas.AtualizadorDeConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.portas.BuscadorDeConsumidor;

/**
 * O caso de uso de atualizar o cadastro de um consumidor: só guarda as portas e declara o papel.
 *
 * <p>
 * A operação está em {@link AtualizacaoDeCadastro}, em cima de {@link #buscador()},
 * {@link #atualizador()} e {@link #saida()}. A classe não é {@code final} porque o Spring cria um
 * proxy que a estende para aplicar a transação.
 */
public class AtualizarConsumidorCaso implements AtualizacaoDeCadastro {

    private final BuscadorDeConsumidor buscador;

    private final AtualizadorDeConsumidor atualizador;

    private final SaidaDeLogPorta saida;

    /**
     * Cria o caso de uso.
     *
     * @param buscador    quem busca o consumidor por id
     * @param atualizador quem grava os dados novos
     * @param saida       a porta de log
     */
    public AtualizarConsumidorCaso(BuscadorDeConsumidor buscador,
            AtualizadorDeConsumidor atualizador, SaidaDeLogPorta saida) {
        this.buscador = buscador;
        this.atualizador = atualizador;
        this.saida = saida;
    }

    @Override
    public BuscadorDeConsumidor buscador() {
        return buscador;
    }

    @Override
    public AtualizadorDeConsumidor atualizador() {
        return atualizador;
    }

    @Override
    public SaidaDeLogPorta saida() {
        return saida;
    }
}
