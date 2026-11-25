package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.criar;

import br.com.alelo.consumer.consumerpat.aplicacao.fakes.SaidaDeLogFake;
import br.com.alelo.consumer.consumerpat.dominio.log.NivelDeLog;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeConsumidores;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsumidorCriadoTeste {

    private final SaidaDeLogFake saida = new SaidaDeLogFake();

    @Test
    void deve_registrar_em_informacao_so_o_id_do_consumidor() {
        // Agir
        ConsumidorCriado.registrar(saida, FabricaDeConsumidores.consumidor(42, 0));

        // Verificar
        assertThat(saida.eventos()).singleElement().satisfies(evento -> {
            assertThat(evento.nivel()).isEqualTo(NivelDeLog.INFORMACAO);
            assertThat(evento.evento()).isEqualTo("consumidor.criado");
            assertThat(evento.campos()).hasSize(1);
            assertThat(evento.valorDe("consumidorId")).contains(42L);
        });
    }

    @Test
    void deve_nao_levar_o_documento_o_nome_nem_o_contato_ao_log() {
        // Agir
        ConsumidorCriado.registrar(saida, FabricaDeConsumidores.consumidor(42, 0));

        // Verificar
        assertThat(saida.eventos().get(0).texto())
                .doesNotContain(FabricaDeConsumidores.DOCUMENTO_PADRAO).doesNotContain("Maria")
                .doesNotContain("11999998888");
    }
}
