package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.criar;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.CartoesDoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;

import java.util.Comparator;
import java.util.Map;

/**
 * Cria os cartões de um consumidor que está sendo cadastrado: um por tipo, com saldo zero.
 *
 * <p>
 * Um cartão novo não tem saldo, e quem pede o cadastro não o escolhe: o saldo só entra por um
 * crédito, que é uma operação com o seu comprovante. Os cartões saem ordenados pelo tipo, para o
 * resultado não depender da ordem em que o mapa foi montado.
 */
public final class CartoesIniciais {

    private CartoesIniciais() {
    }

    /**
     * Cria um cartão de saldo zero para cada número informado.
     *
     * @param numerosCartoes o número do cartão de cada tipo, possivelmente vazio
     * @return os cartões, ordenados pelo tipo
     */
    public static CartoesDoConsumidor de(Map<TipoCartao, NumeroDeCartao> numerosCartoes) {
        return new CartoesDoConsumidor(numerosCartoes.entrySet().stream()
                .map(porTipo -> new Cartao(porTipo.getValue(), porTipo.getKey(), Dinheiro.ZERO))
                .sorted(Comparator.comparing(Cartao::tipo)).toList());
    }
}
