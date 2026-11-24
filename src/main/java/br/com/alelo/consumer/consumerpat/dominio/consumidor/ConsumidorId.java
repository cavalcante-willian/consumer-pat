package br.com.alelo.consumer.consumerpat.dominio.consumidor;

import java.util.Optional;

/**
 * O identificador de um consumidor já gravado.
 *
 * <p>
 * É um tipo próprio para um número de consumidor não ser confundido com outro número qualquer (a
 * versão, o saldo, o id de outra coisa) numa assinatura. Só um consumidor que já existe tem id, e
 * por isso o id é sempre positivo.
 *
 * @param valor o número do id, maior que zero
 */
public record ConsumidorId(long valor) {

    /**
     * Cria o id.
     *
     * @param valor o número do id
     * @throws IllegalArgumentException se o valor não é positivo
     */
    public ConsumidorId {
        valor = Optional.of(valor).filter(candidato -> candidato > 0)
                .orElseThrow(() -> new IllegalArgumentException(
                        "O id do consumidor precisa ser maior que zero"));
    }
}
