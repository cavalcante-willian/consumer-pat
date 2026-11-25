package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.listar;

import br.com.alelo.consumer.consumerpat.dominio.portas.ListadorDeConsumidores;

/**
 * O caso de uso de listar os consumidores: só guarda a porta e declara o papel.
 *
 * <p>
 * A operação está em {@link ListagemDeConsumidores}, em cima de {@link #listador()}. A classe não é
 * {@code final} porque o Spring cria um proxy que a estende para aplicar a transação.
 */
public class ListarConsumidoresCaso implements ListagemDeConsumidores {

    private final ListadorDeConsumidores listador;

    /**
     * Cria o caso de uso.
     *
     * @param listador quem lista os consumidores
     */
    public ListarConsumidoresCaso(ListadorDeConsumidores listador) {
        this.listador = listador;
    }

    @Override
    public ListadorDeConsumidores listador() {
        return listador;
    }
}
