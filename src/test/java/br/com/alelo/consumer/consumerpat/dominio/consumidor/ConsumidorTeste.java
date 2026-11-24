package br.com.alelo.consumer.consumerpat.dominio.consumidor;

import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeConsumidores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsumidorTeste {

    @Test
    void deve_trocar_so_os_dados_quando_atualizar_o_cadastro() {
        // Preparar
        var consumidor = FabricaDeConsumidores.consumidor(5, 3);
        var novosDados = FabricaDeConsumidores.dadosComDocumento("98765432100");

        // Agir
        var atualizado = consumidor.comDados(novosDados);

        // Verificar
        assertThat(atualizado.dados()).isEqualTo(novosDados);
        assertThat(atualizado.id()).isEqualTo(consumidor.id());
        assertThat(atualizado.versao()).isEqualTo(3);
    }

    @Test
    void deve_manter_os_cartoes_e_o_saldo_quando_atualizar_o_cadastro() {
        // Preparar
        var consumidor = FabricaDeConsumidores.consumidor(5, 3);

        // Agir
        var atualizado = consumidor
                .comDados(FabricaDeConsumidores.dadosComDocumento("98765432100"));

        // Verificar
        assertThat(atualizado.cartoes()).isEqualTo(consumidor.cartoes());
    }

    @Test
    void deve_manter_o_original_quando_atualizar_o_cadastro() {
        // Preparar
        var consumidor = FabricaDeConsumidores.consumidor(5, 3);

        // Agir
        consumidor.comDados(FabricaDeConsumidores.dadosComDocumento("98765432100"));

        // Verificar
        assertThat(consumidor.dados()).isEqualTo(FabricaDeConsumidores.dados());
    }

    @Test
    void deve_aceitar_a_versao_zero_quando_o_consumidor_acabou_de_ser_gravado() {
        // Verificar
        assertThat(FabricaDeConsumidores.consumidor(1, 0).versao()).isZero();
    }

    @ParameterizedTest
    @ValueSource(longs = { -1, Long.MIN_VALUE })
    void deve_lancar_quando_a_versao_e_negativa(long versao) {
        // Agir e Verificar
        assertThatThrownBy(() -> FabricaDeConsumidores.consumidor(1, versao))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_lancar_quando_falta_o_id_os_dados_ou_os_cartoes() {
        // Preparar
        var base = FabricaDeConsumidores.consumidor(1, 0);

        // Agir e Verificar
        assertThatThrownBy(() -> new Consumidor(null, base.dados(), base.cartoes(), 0))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Consumidor(base.id(), null, base.cartoes(), 0))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Consumidor(base.id(), base.dados(), null, 0))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deve_nunca_imprimir_o_documento_inteiro_quando_o_consumidor_vai_para_o_log() {
        // Agir
        var texto = FabricaDeConsumidores.consumidor(1, 0).toString();

        // Verificar
        assertThat(texto).doesNotContain(FabricaDeConsumidores.DOCUMENTO_PADRAO)
                .doesNotContain("5555000011112222");
    }
}
