/**
 * O tratador de erros da API: toda exceção vira o corpo de erro padronizado.
 *
 * <p>
 * Há um papel por tipo de exceção, cada um em seu arquivo, e o tratador só os declara. O framework
 * escolhe sempre o mais específico, e o único que captura {@code Exception} é o da falha
 * inesperada.
 */
package br.com.alelo.consumer.consumerpat.api.comum.tratamento;
