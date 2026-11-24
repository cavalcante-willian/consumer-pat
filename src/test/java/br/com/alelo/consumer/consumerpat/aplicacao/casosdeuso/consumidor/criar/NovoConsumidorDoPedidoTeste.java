package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.criar;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoNovoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeConsumidores;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NovoConsumidorDoPedidoTeste {

    @Test
    void deve_levar_o_cadastro_do_pedido_como_esta_quando_montar_o_consumidor() {
        // Preparar
        var pedido = new DtoNovoConsumidor(FabricaDeConsumidores.dados(), Map.of());

        // Agir
        var novo = NovoConsumidorDoPedido.montar(pedido);

        // Verificar
        assertThat(novo.dados()).isEqualTo(FabricaDeConsumidores.dados());
    }

    @Test
    void deve_criar_os_cartoes_de_saldo_zero_quando_o_pedido_traz_numeros() {
        // Preparar
        var numero = new NumeroDeCartao("5555000011112222");
        var pedido = new DtoNovoConsumidor(FabricaDeConsumidores.dados(),
                Map.of(TipoCartao.FARMACIA, numero));

        // Agir
        var novo = NovoConsumidorDoPedido.montar(pedido);

        // Verificar
        assertThat(novo.cartoes())
                .isEqualTo(CartoesIniciais.de(Map.of(TipoCartao.FARMACIA, numero)));
    }
}
