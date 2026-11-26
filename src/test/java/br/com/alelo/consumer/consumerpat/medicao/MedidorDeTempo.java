package br.com.alelo.consumer.consumerpat.medicao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Mede quanto uma ação demora, com o relógio monotônico.
 *
 * <p>
 * A duração é a diferença de {@link System#nanoTime()}, e nunca a de um relógio de parede, que pode
 * andar para trás num ajuste de NTP e dar duração negativa sem erro. A primeira execução paga
 * carregamento de classe e abertura de conexão, então as primeiras são descartadas antes de medir.
 */
public final class MedidorDeTempo {

    private MedidorDeTempo() {
    }

    /**
     * Executa a ação uma vez e mede a duração.
     *
     * @param acao a ação
     * @param <T>  o tipo do resultado
     * @return o resultado e a duração
     * @throws Exception se a ação falha
     */
    public static <T> Cronometrado<T> cronometrar(Callable<T> acao) throws Exception {
        var inicio = System.nanoTime();
        var resultado = acao.call();
        return new Cronometrado<>(System.nanoTime() - inicio, resultado);
    }

    /**
     * Executa a ação sem medir, para aquecer, e depois a executa medindo.
     *
     * @param aquecimento quantas execuções descartar antes de medir
     * @param medicoes    quantas execuções medir
     * @param acao        a ação
     * @param <T>         o tipo do resultado
     * @return uma medição por execução medida, na ordem em que aconteceram
     * @throws Exception se a ação falha
     */
    public static <T> List<Cronometrado<T>> medir(int aquecimento, int medicoes, Callable<T> acao)
            throws Exception {
        for (int i = 0; i < aquecimento; i++) {
            acao.call();
        }
        var medidos = new ArrayList<Cronometrado<T>>();
        for (int i = 0; i < medicoes; i++) {
            medidos.add(cronometrar(acao));
        }
        return medidos;
    }
}
