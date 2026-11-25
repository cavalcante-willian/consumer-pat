package br.com.alelo.consumer.consumerpat.api.consumidor.criar;

import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeConsumidores;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class LocalizacaoDoConsumidorTeste {

    @Test
    void deve_ser_a_rota_do_cadastro_seguida_do_id_quando_montar_o_endereco() {
        // Agir
        var endereco = LocalizacaoDoConsumidor.de("/api/v1/consumidores",
                FabricaDeConsumidores.consumidor(42, 0));

        // Verificar
        assertThat(endereco).isEqualTo(URI.create("/api/v1/consumidores/42"));
    }

    @Test
    void deve_ser_relativo_sem_host_nem_porta_quando_montar_o_endereco() {
        // Agir
        var endereco = LocalizacaoDoConsumidor.de("/api/v1/consumidores",
                FabricaDeConsumidores.consumidor(1, 0));

        // Verificar
        assertThat(endereco.isAbsolute()).isFalse();
        assertThat(endereco.getHost()).isNull();
    }
}
