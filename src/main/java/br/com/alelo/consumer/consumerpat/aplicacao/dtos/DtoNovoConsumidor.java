package br.com.alelo.consumer.consumerpat.aplicacao.dtos;

import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.DadosDoConsumidor;

import java.util.Map;

/**
 * O pedido de cadastrar um consumidor: o cadastro e os números dos cartões que ele traz.
 *
 * <p>
 * Já vem nos tipos do domínio, e quem o monta é a borda, que converte o texto de fora antes de
 * chegar aqui. Os cartões vêm como número por tipo, e não como cartão, porque um cartão novo nasce
 * com saldo zero e quem pede o cadastro não escolhe o saldo.
 *
 * @param dados          o cadastro
 * @param numerosCartoes o número do cartão de cada tipo que o consumidor traz, possivelmente vazio
 */
public record DtoNovoConsumidor(DadosDoConsumidor dados,
        Map<TipoCartao, NumeroDeCartao> numerosCartoes) {
}
