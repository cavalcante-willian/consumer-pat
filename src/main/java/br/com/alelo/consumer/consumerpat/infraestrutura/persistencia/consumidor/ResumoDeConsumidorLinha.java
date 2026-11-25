package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

/**
 * O que a listagem lê de cada consumidor: o id e o nome, e mais nada.
 *
 * <p>
 * É uma projeção, e não a entidade: a consulta seleciona só estas duas colunas, e por isso a
 * listagem não carrega cartões nem endereço e não dispara uma consulta por consumidor.
 *
 * @param id   o identificador
 * @param nome o nome
 */
public record ResumoDeConsumidorLinha(Long id, String nome) {
}
