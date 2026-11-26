package br.com.alelo.consumer.consumerpat.medicao;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CenarioDeCadastroTeste {

    @Test
    void deve_cadastrar_todos_os_consumidores_da_base_quando_medir() throws Exception {
        // Preparar
        var alvo = new AlvoFalso("falso", "800.00");

        // Agir
        new CenarioDeCadastro(7).medir(alvo);

        // Verificar: cada cadastro é uma chamada, e nenhuma é descartada como aquecimento
        assertThat(alvo.cadastros()).isEqualTo(7);
    }

    @Test
    void deve_dar_uma_medida_com_a_vazao_e_os_status_quando_medir() throws Exception {
        // Agir
        var medidas = new CenarioDeCadastro(7).medir(new AlvoFalso("falso", "800.00"));

        // Verificar
        assertThat(medidas).hasSize(1);
        assertThat(medidas.get(0).chave()).isEqualTo("cadastro");
        assertThat(medidas.get(0).descricao()).contains("7 consumidores");
        assertThat(medidas.get(0).resumo()).contains("cadastros/s").contains("status {201=7}");
        assertThat(medidas.get(0).principal()).isPresent();
    }

    @Test
    void deve_cruzar_o_limite_do_bloco_de_avanco_quando_a_base_e_maior_que_um_bloco()
            throws Exception {
        // Preparar: o avanço sai a cada cinco mil, e a base tem um bloco e meio
        var alvo = new AlvoFalso("falso", "800.00");

        // Agir
        new CenarioDeCadastro(7_500).medir(alvo);

        // Verificar
        assertThat(alvo.cadastros()).isEqualTo(7_500);
    }
}
