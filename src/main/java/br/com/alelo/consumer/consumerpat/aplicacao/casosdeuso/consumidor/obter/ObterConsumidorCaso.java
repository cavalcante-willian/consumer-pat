package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.obter;

import br.com.alelo.consumer.consumerpat.dominio.portas.BuscadorDeConsumidor;

/**
 * O caso de uso de ler um consumidor: só guarda a porta e declara o papel.
 *
 * <p>
 * A operação está em {@link LeituraDeConsumidor}, em cima de {@link #buscador()}. A classe não é
 * {@code final} porque o Spring cria um proxy que a estende para aplicar a transação.
 */
public class ObterConsumidorCaso implements LeituraDeConsumidor {

    private final BuscadorDeConsumidor buscador;

    /**
     * Cria o caso de uso.
     *
     * @param buscador quem busca o consumidor por id
     */
    public ObterConsumidorCaso(BuscadorDeConsumidor buscador) {
        this.buscador = buscador;
    }

    @Override
    public BuscadorDeConsumidor buscador() {
        return buscador;
    }
}
