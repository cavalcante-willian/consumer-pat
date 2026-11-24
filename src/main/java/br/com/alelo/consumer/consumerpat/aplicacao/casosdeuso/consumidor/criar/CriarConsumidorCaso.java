package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.criar;

import br.com.alelo.consumer.consumerpat.dominio.portas.GravadorDeNovoConsumidor;

/**
 * O caso de uso de cadastrar um consumidor: só guarda a porta e declara o papel.
 *
 * <p>
 * A operação está em {@link CadastroDeConsumidor}, em cima de {@link #gravador()}. A classe não é
 * {@code final} porque o Spring cria um proxy que a estende para aplicar a transação.
 */
public class CriarConsumidorCaso implements CadastroDeConsumidor {

    private final GravadorDeNovoConsumidor gravador;

    /**
     * Cria o caso de uso.
     *
     * @param gravador quem grava o consumidor novo
     */
    public CriarConsumidorCaso(GravadorDeNovoConsumidor gravador) {
        this.gravador = gravador;
    }

    @Override
    public GravadorDeNovoConsumidor gravador() {
        return gravador;
    }
}
