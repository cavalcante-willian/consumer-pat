package br.com.alelo.consumer.consumerpat.medicao;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MedidorDeTempoTeste {

    @Test
    void deve_devolver_o_resultado_da_acao_quando_cronometrar() throws Exception {
        // Agir
        var cronometrado = MedidorDeTempo.cronometrar(() -> "pronto");

        // Verificar
        assertThat(cronometrado.resultado()).isEqualTo("pronto");
        assertThat(cronometrado.nanos()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void deve_executar_o_aquecimento_e_as_medicoes_mas_so_devolver_as_medicoes_quando_medir()
            throws Exception {
        // Preparar
        var chamadas = new AtomicInteger();

        // Agir
        var medidos = MedidorDeTempo.medir(3, 10, chamadas::incrementAndGet);

        // Verificar: treze execuções, e as três primeiras ficam de fora
        assertThat(chamadas.get()).isEqualTo(13);
        assertThat(medidos).hasSize(10);
        assertThat(medidos.get(0).resultado()).isEqualTo(4);
        assertThat(medidos.get(9).resultado()).isEqualTo(13);
    }

    @Test
    void deve_nao_medir_nada_quando_pedir_zero_medicoes() throws Exception {
        // Agir e Verificar
        assertThat(MedidorDeTempo.medir(2, 0, () -> 1)).isEmpty();
    }

    @Test
    void deve_propagar_a_falha_quando_a_acao_lanca() {
        // Agir e Verificar
        assertThatThrownBy(() -> MedidorDeTempo.medir(0, 1, () -> {
            throw new IllegalStateException("falha da ação");
        })).isInstanceOf(IllegalStateException.class).hasMessage("falha da ação");
    }
}
