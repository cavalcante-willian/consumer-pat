/**
 * O adaptador do consumidor: a busca, a criação e a atualização, com os cartões.
 *
 * <p>
 * É o pacote que compõe a tabela do consumidor e a dos cartões, e por isso o único que conhece as
 * duas. O conflito de documento e de número de cartão é conferido antes de gravar, porque um
 * {@code INSERT} que viola a unicidade lança no meio da transação e a marca para reverter; a
 * unicidade do banco fica como rede de segurança para a corrida entre dois cadastros.
 */
package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cadastro;
