package br.com.alelo.consumer.consumerpat.medicao;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AmostraDeTemposTeste {

    private static final long MILISSEGUNDO = 1_000_000L;

    @Test
    void deve_tomar_a_duracao_do_meio_quando_a_quantidade_e_impar() {
        // Preparar: fora de ordem, para provar que a amostra ordena
        var amostra = new AmostraDeTempos(
                List.of(5 * MILISSEGUNDO, MILISSEGUNDO, 3 * MILISSEGUNDO));

        // Agir e Verificar
        assertThat(amostra.mediana()).isEqualTo(3.0);
    }

    @Test
    void deve_tomar_a_media_das_duas_do_meio_quando_a_quantidade_e_par() {
        // Preparar
        var amostra = new AmostraDeTempos(
                List.of(MILISSEGUNDO, 2 * MILISSEGUNDO, 4 * MILISSEGUNDO, 100 * MILISSEGUNDO));

        // Agir e Verificar
        assertThat(amostra.mediana()).isEqualTo(3.0);
    }

    @Test
    void deve_nao_se_deixar_arrastar_pela_execucao_lenta_quando_tirar_a_mediana() {
        // Preparar: uma coleta de lixo no meio da medição
        var amostra = new AmostraDeTempos(List.of(MILISSEGUNDO, MILISSEGUNDO, MILISSEGUNDO,
                900 * MILISSEGUNDO, MILISSEGUNDO));

        // Agir e Verificar
        assertThat(amostra.mediana()).isEqualTo(1.0);
        assertThat(amostra.maximo()).isEqualTo(900.0);
    }

    @Test
    void deve_dar_o_minimo_e_o_maximo_e_a_soma_quando_perguntar() {
        // Preparar
        var amostra = new AmostraDeTempos(
                List.of(2 * MILISSEGUNDO, 8 * MILISSEGUNDO, 5 * MILISSEGUNDO));

        // Agir e Verificar
        assertThat(amostra.minimo()).isEqualTo(2.0);
        assertThat(amostra.maximo()).isEqualTo(8.0);
        assertThat(amostra.total()).isEqualTo(15.0);
        assertThat(amostra.quantidade()).isEqualTo(3);
    }

    @Test
    void deve_cobrir_noventa_e_cinco_por_cento_quando_tirar_o_percentil() {
        // Preparar: 1 ms, 2 ms, ..., 100 ms
        var amostra = new AmostraDeTempos(
                LongStream.rangeClosed(1, 100).map(i -> i * MILISSEGUNDO).boxed().toList());

        // Agir e Verificar: noventa e cinco das cem durações são menores ou iguais a 95 ms
        assertThat(amostra.percentil95()).isEqualTo(95.0);
    }

    @Test
    void deve_dar_a_unica_duracao_como_tudo_quando_a_amostra_tem_uma() {
        // Preparar
        var amostra = new AmostraDeTempos(List.of(7 * MILISSEGUNDO));

        // Agir e Verificar
        assertThat(amostra.mediana()).isEqualTo(7.0);
        assertThat(amostra.percentil95()).isEqualTo(7.0);
    }

    @Test
    void deve_recusar_a_amostra_vazia_quando_construir() {
        // Agir e Verificar
        assertThatThrownBy(() -> new AmostraDeTempos(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_escrever_o_resumo_com_virgula_decimal_quando_resumir() {
        // Preparar
        var amostra = new AmostraDeTempos(List.of(1_500_000L));

        // Agir e Verificar
        assertThat(amostra.resumo())
                .isEqualTo("mediana 1,50 ms | p95 1,50 ms | mín 1,50 ms | máx 1,50 ms");
    }
}
