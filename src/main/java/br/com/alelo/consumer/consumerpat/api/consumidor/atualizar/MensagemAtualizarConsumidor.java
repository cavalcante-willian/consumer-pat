package br.com.alelo.consumer.consumerpat.api.consumidor.atualizar;

import br.com.alelo.consumer.consumerpat.api.consumidor.MensagemDadosConsumidor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

/**
 * O corpo da atualização: a versão que o cliente leu e os dados cadastrais como devem ficar.
 *
 * <p>
 * Não há campo de saldo nem de cartão: o Jackson recusa o que não está aqui, e é isso que garante
 * que atualizar um cadastro não mexe em dinheiro. A versão é a que a leitura devolveu, e a que não
 * é mais a atual faz a atualização responder {@code 409}.
 *
 * @param versao a versão do consumidor que o cliente leu
 * @param dados  os dados cadastrais como devem ficar
 */
public record MensagemAtualizarConsumidor(@NotNull @PositiveOrZero Long versao,
        @NotNull @Valid MensagemDadosConsumidor dados) {
}
