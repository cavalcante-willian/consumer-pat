package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao;

/**
 * Papel de um adaptador que tem o repositório dos cartões: é a operação-base de que as operações
 * dele são escritas.
 */
interface ComRepositorioDeCartoes {

    /**
     * O repositório dos cartões.
     *
     * @return o repositório do Spring Data
     */
    CartaoRepositorioJpa repositorioDeCartoes();
}
