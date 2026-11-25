package br.com.alelo.consumer.consumerpat.dominio.pagina;

/**
 * Por que campo a listagem de consumidores pode ser ordenada.
 *
 * <p>
 * É um conjunto fechado, e nunca o nome de uma coluna digitado pelo cliente: o que o cliente
 * escolhe é um destes valores, e o que cada um significa no banco é do adaptador. Toda ordenação
 * desempata por id, porque sem desempate dois consumidores com o mesmo nome podem trocar de lugar
 * entre duas consultas, e a paginação pula ou repete item.
 */
public enum OrdenacaoDeConsumidor {

    /** Pelo identificador. */
    ID,

    /** Pelo nome. */
    NOME
}
