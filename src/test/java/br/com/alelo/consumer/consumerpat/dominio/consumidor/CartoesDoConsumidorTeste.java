package br.com.alelo.consumer.consumerpat.dominio.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeCartoes;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartoesDoConsumidorTeste {

    private static Cartao cartao(TipoCartao tipo, String numero) {
        return FabricaDeCartoes.doTipo(tipo).comNumero(numero).construir();
    }

    @Test
    void deve_aceitar_um_cartao_de_cada_tipo() {
        // Agir
        var cartoes = new CartoesDoConsumidor(
                List.of(cartao(TipoCartao.ALIMENTACAO, "5555000011112222"),
                        cartao(TipoCartao.FARMACIA, "5555000011113333"),
                        cartao(TipoCartao.COMBUSTIVEL, "5555000011114444")));

        // Verificar
        assertThat(cartoes.cartoes()).hasSize(3);
    }

    @Test
    void deve_aceitar_nenhum_cartao() {
        // Verificar
        assertThat(new CartoesDoConsumidor(List.of()).cartoes()).isEmpty();
    }

    @Test
    void deve_lancar_quando_ha_dois_cartoes_do_mesmo_tipo() {
        // Agir e Verificar
        assertThatThrownBy(() -> new CartoesDoConsumidor(
                List.of(cartao(TipoCartao.FARMACIA, "5555000011112222"),
                        cartao(TipoCartao.FARMACIA, "5555000011113333"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_lancar_quando_a_lista_e_nula() {
        // Agir e Verificar
        assertThatThrownBy(() -> new CartoesDoConsumidor(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_guardar_uma_copia_quando_a_lista_original_muda_depois() {
        // Preparar
        var original = new ArrayList<>(List.of(cartao(TipoCartao.ALIMENTACAO, "5555000011112222")));
        var cartoes = new CartoesDoConsumidor(original);

        // Agir
        original.add(cartao(TipoCartao.FARMACIA, "5555000011113333"));

        // Verificar
        assertThat(cartoes.cartoes()).hasSize(1);
    }

    @Test
    void deve_ser_imutavel_quando_alguem_tenta_alterar_a_lista_devolvida() {
        // Preparar
        var cartoes = new CartoesDoConsumidor(
                List.of(cartao(TipoCartao.ALIMENTACAO, "5555000011112222")));

        // Agir e Verificar
        assertThatThrownBy(() -> cartoes.cartoes().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
