/**
 * A especificação OpenAPI da API: o cabeçalho, os grupos de operações e o que é comum às respostas.
 *
 * <p>
 * Cada controller declara o que é só dele, com as anotações do Swagger: o resumo, o sucesso e os
 * erros de cada operação. O que vale para todas, o corpo dos erros, o {@code 500} e o
 * {@code X-Request-Id}, é completado uma vez, aqui, para nenhum controller novo se esquecer.
 */
package br.com.alelo.consumer.consumerpat.api.comum.documentacao;
