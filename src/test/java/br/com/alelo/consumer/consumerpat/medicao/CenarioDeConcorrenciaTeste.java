package br.com.alelo.consumer.consumerpat.medicao;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CenarioDeConcorrenciaTeste {

    @Test
    void deve_disputar_vinte_compras_em_cada_uma_das_cinco_rodadas_quando_medir() throws Exception {
        // Preparar
        var alvo = new AlvoFalso("falso", "800.00");

        // Agir
        new CenarioDeConcorrencia().medir(alvo);

        // Verificar
        assertThat(alvo.comprasNaFarmacia()).isEqualTo(100);
    }

    @Test
    void deve_dizer_que_o_saldo_foi_exato_quando_o_sistema_termina_em_oitocentos()
            throws Exception {
        // Agir
        var medidas = new CenarioDeConcorrencia().medir(new AlvoFalso("novo", "800.00"));

        // Verificar
        var saldo = medidas.get(0);
        assertThat(saldo.chave()).isEqualTo("concorrencia-saldo-final");
        assertThat(saldo.resumo()).contains("exato (800.00) em 5 de 5 rodadas")
                .contains("[800.00, 800.00, 800.00, 800.00, 800.00]").contains("status {201=100}");
        assertThat(saldo.principal()).isEmpty();
    }

    @Test
    void deve_dizer_que_o_saldo_se_perdeu_quando_o_sistema_termina_acima_de_oitocentos()
            throws Exception {
        // Agir: o legado perde atualizações e termina com mais saldo do que devia
        var medidas = new CenarioDeConcorrencia().medir(new AlvoFalso("legado", "830.00"));

        // Verificar
        assertThat(medidas.get(0).resumo()).contains("exato (800.00) em 0 de 5 rodadas")
                .contains("830.00");
    }

    @Test
    void deve_medir_o_tempo_e_a_vazao_das_compras_simultaneas_quando_medir() throws Exception {
        // Agir
        var tempo = new CenarioDeConcorrencia().medir(new AlvoFalso("novo", "800.00")).get(1);

        // Verificar
        assertThat(tempo.chave()).isEqualTo("concorrencia-tempo");
        assertThat(tempo.resumo()).contains("mediana").contains("compras/s na mediana");
        assertThat(tempo.principal()).isPresent();
    }
}
