package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

/**
 * Operação-base dos papéis do adaptador do consumidor: o repositório dos consumidores.
 */
public interface ComRepositorioDeConsumidores {

    /**
     * O repositório dos consumidores.
     *
     * @return o repositório
     */
    ConsumidorRepositorioJpa repositorioDeConsumidores();
}
