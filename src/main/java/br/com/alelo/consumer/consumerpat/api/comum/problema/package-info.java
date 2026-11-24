/**
 * O corpo de erro da API e como ele se monta.
 *
 * <p>
 * O corpo é o de problem details (RFC 7807). Um erro de domínio traz o código e o status da própria
 * variante e da categoria, e um problema de borda traz os seus de um enum: nada disso é escolhido
 * por quem monta o corpo.
 */
package br.com.alelo.consumer.consumerpat.api.comum.problema;
