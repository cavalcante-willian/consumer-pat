package br.com.alelo.consumer.consumerpat.medicao;

import java.util.List;
import java.util.Locale;

/**
 * Um conjunto de durações e o que se diz dele: mediana, mínimo, máximo e o percentil 95.
 *
 * <p>
 * A mediana é o número que se compara, porque uma execução lenta por coleta de lixo ou por
 * escalonador entorta a média e não entorta a mediana. As durações entram em nanossegundos e saem
 * em milissegundos.
 */
public final class AmostraDeTempos {

    private static final Locale PORTUGUES = Locale.forLanguageTag("pt-BR");

    private static final double NANOS_POR_MILISSEGUNDO = 1_000_000.0;

    private final List<Long> ordenadas;

    /**
     * Cria a amostra a partir das durações, em qualquer ordem.
     *
     * @param nanos as durações em nanossegundos, ao menos uma
     * @throws IllegalArgumentException se não há nenhuma duração
     */
    public AmostraDeTempos(List<Long> nanos) {
        this.ordenadas = nanos.stream().sorted().toList();
        this.ordenadas.stream().findAny().orElseThrow(
                () -> new IllegalArgumentException("A amostra precisa de ao menos uma duração"));
    }

    /**
     * A quantidade de durações.
     *
     * @return quantas execuções a amostra tem
     */
    public int quantidade() {
        return ordenadas.size();
    }

    /**
     * A mediana: a duração do meio, ou a média das duas do meio quando a quantidade é par.
     *
     * @return a mediana, em milissegundos
     */
    public double mediana() {
        var abaixo = ordenadas.get((ordenadas.size() - 1) / 2);
        var acima = ordenadas.get(ordenadas.size() / 2);
        return (abaixo + acima) / 2.0 / NANOS_POR_MILISSEGUNDO;
    }

    /**
     * A menor duração.
     *
     * @return o mínimo, em milissegundos
     */
    public double minimo() {
        return ordenadas.get(0) / NANOS_POR_MILISSEGUNDO;
    }

    /**
     * A maior duração.
     *
     * @return o máximo, em milissegundos
     */
    public double maximo() {
        return ordenadas.get(ordenadas.size() - 1) / NANOS_POR_MILISSEGUNDO;
    }

    /**
     * O percentil 95, pelo método do posto mais próximo: a menor duração que cobre 95% da amostra.
     *
     * @return o percentil 95, em milissegundos
     */
    public double percentil95() {
        var posto = (int) Math.ceil(0.95 * ordenadas.size()) - 1;
        return ordenadas.get(posto) / NANOS_POR_MILISSEGUNDO;
    }

    /**
     * A soma de todas as durações.
     *
     * @return o total, em milissegundos
     */
    public double total() {
        return ordenadas.stream().mapToLong(Long::longValue).sum() / NANOS_POR_MILISSEGUNDO;
    }

    /**
     * A amostra numa linha, para o relatório.
     *
     * @return mediana, percentil 95, mínimo e máximo, em milissegundos
     */
    public String resumo() {
        return String.format(PORTUGUES, "mediana %.2f ms | p95 %.2f ms | mín %.2f ms | máx %.2f ms",
                mediana(), percentil95(), minimo(), maximo());
    }
}
