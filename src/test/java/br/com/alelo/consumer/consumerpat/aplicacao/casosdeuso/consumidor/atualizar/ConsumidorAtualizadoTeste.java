package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.atualizar;

import br.com.alelo.consumer.consumerpat.aplicacao.fakes.SaidaDeLogFake;
import br.com.alelo.consumer.consumerpat.dominio.log.NivelDeLog;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeConsumidores;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsumidorAtualizadoTeste {

    private final SaidaDeLogFake saida = new SaidaDeLogFake();

    @Test
    void deve_registrar_em_informacao_o_id_e_a_versao_como_ficou() {
        // Agir
        ConsumidorAtualizado.registrar(saida, FabricaDeConsumidores.consumidor(7, 4));

        // Verificar
        assertThat(saida.eventos()).singleElement().satisfies(evento -> {
            assertThat(evento.nivel()).isEqualTo(NivelDeLog.INFORMACAO);
            assertThat(evento.evento()).isEqualTo("consumidor.atualizado");
            assertThat(evento.campos()).hasSize(2);
            assertThat(evento.valorDe("consumidorId")).contains(7L);
            assertThat(evento.valorDe("versao")).contains(4L);
        });
    }

    @Test
    void deve_nao_levar_o_cadastro_ao_log() {
        // Agir
        ConsumidorAtualizado.registrar(saida, FabricaDeConsumidores.consumidor(7, 4));

        // Verificar
        assertThat(saida.eventos().get(0).texto())
                .doesNotContain(FabricaDeConsumidores.DOCUMENTO_PADRAO).doesNotContain("Maria");
    }
}
