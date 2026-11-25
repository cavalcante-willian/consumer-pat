/**
 * Os endpoints do consumidor, um pacote por operação: {@code criar}, {@code atualizar} e
 * {@code obter}.
 *
 * <p>
 * Fica aqui o que as operações compartilham: os dados cadastrais da entrada, que o cadastro e a
 * atualização leem do mesmo jeito, e a mensagem de saída. A leitura dos campos é uma lista de
 * resultados independentes, e as falhas de todos saem num só erro, em vez de parar na primeira. O
 * que sai da API é mascarado: documento, telefones e números de cartão.
 */
package br.com.alelo.consumer.consumerpat.api.consumidor;
