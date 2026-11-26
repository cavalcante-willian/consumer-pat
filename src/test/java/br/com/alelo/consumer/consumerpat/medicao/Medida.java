package br.com.alelo.consumer.consumerpat.medicao;

import java.util.OptionalDouble;

/**
 * O que se mediu de um sistema numa chamada, pronto para o relatório.
 *
 * @param chave     o nome estável da chamada, igual nos dois sistemas, pelo qual o relatório junta
 *                  o legado e o novo na mesma linha
 * @param descricao o que a chamada fez neste sistema
 * @param resumo    o resultado em texto: tempos, tamanho da resposta e status
 * @param principal o número que se compara entre os sistemas, em milissegundos, se houver um
 */
public record Medida(String chave, String descricao, String resumo, OptionalDouble principal) {
}
