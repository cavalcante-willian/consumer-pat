/**
 * O que a API faz com a requisição antes e depois do controller: o id, a correlação e a conclusão.
 *
 * <p>
 * É onde mora a única exceção declarada à regra de que só {@code infraestrutura.log} importa o
 * SLF4J: o {@code MDC}, que carrega o id de cada requisição até o padrão do log.
 */
package br.com.alelo.consumer.consumerpat.api.comum.requisicao;
