package br.com.alelo.consumer.consumerpat.medicao;

/**
 * O resultado de uma execução e quanto ela durou.
 *
 * @param nanos     a duração, em nanossegundos, medida com o relógio monotônico
 * @param resultado o que a execução devolveu
 * @param <T>       o tipo do resultado
 */
public record Cronometrado<T>(long nanos, T resultado) {
}
