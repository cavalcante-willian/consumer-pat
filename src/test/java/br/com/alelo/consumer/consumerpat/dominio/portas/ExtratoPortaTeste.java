package br.com.alelo.consumer.consumerpat.dominio.portas;

import br.com.alelo.consumer.consumerpat.dominio.extrato.Extrato;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ExtratoPortaTeste {

    @Test
    void deve_oferecer_so_registrar_quando_listar_as_operacoes_da_porta() {
        // Agir
        var operacoes = Arrays.stream(ExtratoPorta.class.getMethods()).map(Method::getName)
                .toList();

        // Verificar
        assertThat(operacoes).containsExactly("registrar");
    }

    @Test
    void deve_registrar_um_extrato_quando_consultar_o_parametro_da_operacao() throws Exception {
        // Agir
        var registrar = ExtratoPorta.class.getMethod("registrar", Extrato.class);

        // Verificar
        assertThat(registrar.getReturnType()).isEqualTo(void.class);
    }
}
