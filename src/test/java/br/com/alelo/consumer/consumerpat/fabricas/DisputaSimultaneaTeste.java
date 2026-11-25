package br.com.alelo.consumer.consumerpat.fabricas;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisputaSimultaneaTeste {

    private static final int TAREFAS = 8;

    @Test
    void deve_largar_todas_as_tarefas_ao_mesmo_tempo_quando_disputar() throws Exception {
        // Preparar: a barreira só abre quando as oito estão dentro dela ao mesmo tempo, então uma
        // execução em série estouraria o prazo
        var barreira = new CyclicBarrier(TAREFAS);
        List<Callable<Integer>> tarefas = IntStream.range(0, TAREFAS)
                .<Callable<Integer>>mapToObj(i -> () -> {
                    barreira.await(5, TimeUnit.SECONDS);
                    return i;
                }).toList();

        // Agir e Verificar
        assertThat(DisputaSimultanea.disputar(tarefas)).hasSize(TAREFAS);
    }

    @Test
    void deve_devolver_os_resultados_na_ordem_das_tarefas_quando_disputar() throws Exception {
        // Preparar
        List<Callable<Integer>> tarefas = IntStream.range(0, TAREFAS)
                .<Callable<Integer>>mapToObj(i -> () -> i * 10).toList();

        // Agir e Verificar
        assertThat(DisputaSimultanea.disputar(tarefas)).containsExactly(0, 10, 20, 30, 40, 50, 60,
                70);
    }

    @Test
    void deve_propagar_a_falha_da_tarefa_quando_ela_lanca() {
        // Preparar
        List<Callable<Integer>> tarefas = List.of(() -> {
            throw new IllegalStateException("falha da tarefa");
        });

        // Agir e Verificar
        assertThatThrownBy(() -> DisputaSimultanea.disputar(tarefas))
                .isInstanceOf(ExecutionException.class).hasRootCauseMessage("falha da tarefa");
    }

    @Test
    void deve_falhar_no_prazo_quando_uma_tarefa_nao_termina() {
        // Preparar: a tarefa espera uma trava que ninguém abre, e a interrupção do pool a solta
        var nunca = new CountDownLatch(1);
        List<Callable<Integer>> tarefas = List.of(() -> {
            nunca.await();
            return 1;
        });

        // Agir e Verificar
        assertThatThrownBy(() -> DisputaSimultanea.disputar(Duration.ofMillis(200), tarefas))
                .isInstanceOf(TimeoutException.class);
    }
}
