package br.com.alelo.consumer.consumerpat.dominio.consumidor;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsumidorIdTeste {

    @Test
    void deve_guardar_o_valor_quando_ele_e_positivo() {
        // Agir
        var id = new ConsumidorId(42);

        // Verificar
        assertThat(id.valor()).isEqualTo(42);
    }

    @ParameterizedTest
    @ValueSource(longs = { 0, -1, Long.MIN_VALUE })
    void deve_lancar_quando_o_valor_nao_e_positivo(long valor) {
        // Agir e Verificar
        assertThatThrownBy(() -> new ConsumidorId(valor))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_ser_igual_quando_o_valor_e_o_mesmo() {
        // Verificar
        assertThat(new ConsumidorId(7)).isEqualTo(new ConsumidorId(7))
                .isNotEqualTo(new ConsumidorId(8));
    }
}
