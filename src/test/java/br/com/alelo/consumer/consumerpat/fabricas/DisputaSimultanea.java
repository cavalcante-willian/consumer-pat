package br.com.alelo.consumer.consumerpat.fabricas;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Executa tarefas na mesma largada e devolve o resultado de cada uma, na ordem em que foram dadas.
 *
 * <p>
 * Um teste de concorrência só prova alguma coisa se as tarefas disputam de verdade: sem largada, as
 * threads podem rodar uma depois da outra e o teste passa sem testar nada. Por isso cada tarefa
 * espera numa trava até que todas estejam prontas, e só então a trava abre. Toda espera tem prazo,
 * de modo que um impasse vira falha clara em vez de um build pendurado, e nada aqui espera por um
 * tempo fixo.
 *
 * <p>
 * Quem usa confere a invariante do resultado, nunca a ordem em que as tarefas terminaram.
 */
public final class DisputaSimultanea {

    private static final Duration PRAZO_PADRAO = Duration.ofSeconds(10);

    private DisputaSimultanea() {
    }

    /**
     * Executa as tarefas na mesma largada, com o prazo padrão de dez segundos.
     *
     * @param tarefas as tarefas, uma por thread
     * @param <T>     o resultado de cada tarefa
     * @return o resultado de cada tarefa, na ordem das tarefas
     * @throws Exception se uma tarefa falha ou o prazo se esgota
     */
    public static <T> List<T> disputar(List<Callable<T>> tarefas) throws Exception {
        return disputar(PRAZO_PADRAO, tarefas);
    }

    /**
     * Executa as tarefas na mesma largada, com o prazo informado para cada espera.
     *
     * @param prazo   o prazo de cada espera: a chegada de todas à largada e o fim de cada tarefa
     * @param tarefas as tarefas, uma por thread
     * @param <T>     o resultado de cada tarefa
     * @return o resultado de cada tarefa, na ordem das tarefas
     * @throws Exception se uma tarefa falha ou o prazo se esgota
     */
    public static <T> List<T> disputar(Duration prazo, List<Callable<T>> tarefas) throws Exception {
        var prontas = new CountDownLatch(tarefas.size());
        var largada = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(tarefas.size());
        try {
            var futuros = tarefas.stream().map(tarefa -> pool.submit(() -> {
                prontas.countDown();
                largada.await();
                return tarefa.call();
            })).toList();
            assertThat(prontas.await(prazo.toMillis(), TimeUnit.MILLISECONDS))
                    .as("todas as tarefas chegaram à largada").isTrue();
            largada.countDown();
            var resultados = new ArrayList<T>();
            for (var futuro : futuros) {
                resultados.add(futuro.get(prazo.toMillis(), TimeUnit.MILLISECONDS));
            }
            return resultados;
        } finally {
            pool.shutdownNow();
        }
    }
}
