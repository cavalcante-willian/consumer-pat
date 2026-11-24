/**
 * O que é comum a todos os endpoints, dividido pelo que muda junto: {@code problema} (o corpo de
 * erro), {@code resposta} (a resposta HTTP), {@code campos} (a tradução dos campos com erro) e
 * {@code tratamento} (o tratador de exceções).
 *
 * <p>
 * Há um só formato de erro, o de problem details (RFC 7807), e um só lugar que decide o status: o
 * erro de domínio o traz da própria categoria, e o de borda o traz do seu enum. Nenhum controller
 * escolhe status nem monta corpo de erro.
 */
package br.com.alelo.consumer.consumerpat.api.comum;
