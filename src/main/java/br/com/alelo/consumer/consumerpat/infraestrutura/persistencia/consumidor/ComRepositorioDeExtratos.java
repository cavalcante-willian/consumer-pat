package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

/**
 * Papel de um adaptador que tem o repositório dos extratos: é a operação-base de que as operações
 * dele são escritas.
 */
interface ComRepositorioDeExtratos {

    /**
     * O repositório dos extratos.
     *
     * @return o repositório do Spring Data
     */
    ExtratoRepositorioJpa repositorioDeExtratos();
}
