package br.com.alelo.consumer.consumerpat.dominio.consumidor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResumoDeConsumidorTeste {

    @Test
    void deve_guardar_o_id_e_o_nome_quando_criar() {
        // Agir
        var resumo = new ResumoDeConsumidor(new ConsumidorId(3), "Maria Souza");

        // Verificar
        assertThat(resumo.id()).isEqualTo(new ConsumidorId(3));
        assertThat(resumo.nome()).isEqualTo("Maria Souza");
    }

    @Test
    void deve_lancar_quando_falta_o_id_ou_o_nome() {
        // Agir e Verificar
        assertThatThrownBy(() -> new ResumoDeConsumidor(null, "Maria"))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ResumoDeConsumidor(new ConsumidorId(1), null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deve_nao_ter_cartoes_nem_endereco_quando_consultar_os_componentes() {
        // Verificar: a listagem não carrega o que não mostra
        assertThat(ResumoDeConsumidor.class.getRecordComponents())
                .extracting(componente -> componente.getName()).containsExactly("id", "nome");
    }
}
