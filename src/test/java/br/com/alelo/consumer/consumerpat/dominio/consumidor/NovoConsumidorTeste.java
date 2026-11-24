package br.com.alelo.consumer.consumerpat.dominio.consumidor;

import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeConsumidores;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NovoConsumidorTeste {

    @Test
    void deve_guardar_os_dados_e_os_cartoes_quando_criar() {
        // Agir
        var novo = FabricaDeConsumidores.novo();

        // Verificar
        assertThat(novo.dados()).isEqualTo(FabricaDeConsumidores.dados());
        assertThat(novo.cartoes().cartoes()).hasSize(1);
    }

    @Test
    void deve_aceitar_um_consumidor_sem_cartoes() {
        // Agir
        var novo = new NovoConsumidor(FabricaDeConsumidores.dados(),
                new CartoesDoConsumidor(List.of()));

        // Verificar
        assertThat(novo.cartoes().cartoes()).isEmpty();
    }

    @Test
    void deve_lancar_quando_faltam_os_dados_ou_os_cartoes() {
        // Agir e Verificar
        assertThatThrownBy(() -> new NovoConsumidor(null, new CartoesDoConsumidor(List.of())))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new NovoConsumidor(FabricaDeConsumidores.dados(), null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deve_nao_ter_id_nem_versao_quando_consultar_os_componentes() {
        // Verificar
        assertThat(NovoConsumidor.class.getRecordComponents())
                .extracting(componente -> componente.getName()).containsExactly("dados", "cartoes");
    }
}
