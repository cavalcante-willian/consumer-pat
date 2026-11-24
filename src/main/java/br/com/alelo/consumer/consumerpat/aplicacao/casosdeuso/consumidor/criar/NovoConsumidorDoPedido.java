package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.criar;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoNovoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.NovoConsumidor;

/**
 * Monta o consumidor a gravar a partir do pedido de cadastro.
 *
 * <p>
 * É a conta pura do caso de uso de criar, separada dele para ser exercitada sem porta: o cadastro
 * vem do pedido como está, e os cartões nascem de {@link CartoesIniciais}.
 */
public final class NovoConsumidorDoPedido {

    private NovoConsumidorDoPedido() {
    }

    /**
     * Monta o consumidor ainda não gravado.
     *
     * @param pedido o pedido de cadastro
     * @return o consumidor, com os cartões de saldo zero
     */
    public static NovoConsumidor montar(DtoNovoConsumidor pedido) {
        return new NovoConsumidor(pedido.dados(), CartoesIniciais.de(pedido.numerosCartoes()));
    }
}
