package br.com.alelo.consumer.consumerpat.aplicacao.fakes;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * O estado que o fake do cartão compartilha entre as suas operações: os cartões que existem e, à
 * parte, os que foram gravados durante o teste.
 *
 * <p>
 * Os cartões iniciais entram só no primeiro mapa, para que um teste possa afirmar que um pedido
 * recusado não gravou nada. Não tem operação: as operações são papéis, escritos em cima dele.
 */
public final class CartoesEmMemoria {

    private final Map<ChaveDeCartao, Cartao> cartoes = new HashMap<>();

    private final List<Cartao> gravacoes = new ArrayList<>();

    /**
     * Os cartões que existem, por chave. É o mapa vivo, e não uma cópia.
     *
     * @return o mapa de cartões
     */
    public Map<ChaveDeCartao, Cartao> cartoes() {
        return cartoes;
    }

    /**
     * Os cartões gravados durante o teste, em ordem. É a lista viva, e não uma cópia.
     *
     * @return a lista de gravações
     */
    public List<Cartao> gravacoes() {
        return gravacoes;
    }
}
