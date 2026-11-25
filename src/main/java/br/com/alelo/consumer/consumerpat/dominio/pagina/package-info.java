/**
 * A paginação das listagens: a página, o pedido e a leitura dos parâmetros.
 *
 * <p>
 * Não depende de nenhuma classe de paginação do Spring: a página começa em 1, como o contrato da
 * API, e a conversão para o que o banco espera é do adaptador. Os parâmetros são lidos juntos, e
 * todos os erros saem de uma vez.
 */
package br.com.alelo.consumer.consumerpat.dominio.pagina;
