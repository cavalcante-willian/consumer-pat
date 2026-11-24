package br.com.alelo.consumer.consumerpat.aplicacao.dtos;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.DadosDoConsumidor;

/**
 * O pedido de atualizar o cadastro de um consumidor.
 *
 * <p>
 * Traz a versão que o cliente leu, e é com ela que a atualização sabe se outro cliente escreveu no
 * meio. Não traz cartões nem saldo: o tipo não tem por onde carregá-los, e é por isso que atualizar
 * um cadastro não consegue mexer em dinheiro.
 *
 * @param id     o consumidor a atualizar
 * @param versao a versão do consumidor que o cliente leu
 * @param dados  o cadastro como deve ficar
 */
public record DtoAtualizacaoDeConsumidor(ConsumidorId id, long versao, DadosDoConsumidor dados) {
}
