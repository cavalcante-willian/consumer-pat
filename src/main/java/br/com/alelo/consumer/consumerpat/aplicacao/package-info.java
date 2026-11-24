/**
 * Camada de aplicação: os casos de uso, que orquestram o domínio e as portas.
 *
 * <p>
 * Um caso de uso recebe um DTO de entrada, converte os textos nos tipos do domínio, chama as regras
 * e devolve um {@code Resultado}. Depende só do domínio, com uma exceção documentada: a anotação de
 * fronteira de transação do Spring, que não carrega nenhuma implementação.
 */
package br.com.alelo.consumer.consumerpat.aplicacao;
