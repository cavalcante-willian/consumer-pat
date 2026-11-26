package br.com.alelo.consumer.consumerpat.seguranca;

/**
 * O que um sistema mostra de um consumidor cujo documento e cartão a sonda conhece.
 *
 * @param texto     o que o sistema devolveu ao ler o consumidor
 * @param documento o documento cadastrado, por inteiro
 * @param cartao    o número de um cartão cadastrado, por inteiro
 */
public record ExposicaoDeDados(String texto, String documento, String cartao) {
}
