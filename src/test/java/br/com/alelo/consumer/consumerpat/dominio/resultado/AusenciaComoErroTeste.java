package br.com.alelo.consumer.consumerpat.dominio.resultado;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class AusenciaComoErroTeste {

    @Test
    void deve_ser_sucesso_com_o_valor_quando_ha_valor() {
        // Preparar
        var opcional = Optional.of("achado");

        // Agir
        var resultado = AusenciaComoErro.converter(opcional, () -> "ausente");

        // Verificar
        assertThat(resultado).isEqualTo(new Sucesso<String, String>("achado"));
    }

    @Test
    void deve_ser_falha_com_o_erro_fornecido_quando_ha_ausencia() {
        // Preparar
        Optional<String> opcional = Optional.empty();

        // Agir
        var resultado = AusenciaComoErro.converter(opcional, () -> "ausente");

        // Verificar
        assertThat(resultado).isEqualTo(new Falha<String, String>("ausente"));
    }

    @Test
    void deve_nao_pedir_o_erro_quando_ha_valor() {
        // Preparar
        var pedidos = new AtomicInteger();

        // Agir
        AusenciaComoErro.converter(Optional.of("achado"),
                () -> "erro " + pedidos.incrementAndGet());

        // Verificar
        assertThat(pedidos.get()).isZero();
    }
}
