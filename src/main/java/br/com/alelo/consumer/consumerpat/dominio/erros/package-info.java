/**
 * Erros de negócio, modelados como dado.
 *
 * <p>
 * Um erro de domínio é um {@code record} que implementa {@link
 * br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio}: nunca uma
 * exceção. Ele viaja dentro de
 * um {@code Resultado} e carrega o que o caso exige para explicar o que houve.
 * Os erros são
 * agrupados por categoria de falha, cada categoria num subpacote, e cada
 * variante concreta tem um
 * código estável.
 */
package br.com.alelo.consumer.consumerpat.dominio.erros;
