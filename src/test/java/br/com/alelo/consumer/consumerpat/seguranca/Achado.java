package br.com.alelo.consumer.consumerpat.seguranca;

/**
 * O que uma verificação concluiu sobre um sistema.
 *
 * @param chave     o nome estável da verificação, igual nos dois sistemas, pelo qual o relatório
 *                  junta o legado e o novo na mesma linha
 * @param descricao o que a verificação tenta
 * @param protegido {@code true} se o sistema se defendeu, {@code false} se deixou passar
 * @param evidencia o que foi observado: o status, o efeito no saldo, o que a resposta continha
 */
public record Achado(String chave, String descricao, boolean protegido, String evidencia) {
}
