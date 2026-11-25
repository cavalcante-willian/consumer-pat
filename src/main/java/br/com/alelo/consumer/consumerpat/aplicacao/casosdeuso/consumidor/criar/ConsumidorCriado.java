package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.criar;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.log.SaidaDeLogPorta;

import static br.com.alelo.consumer.consumerpat.dominio.log.CampoDeLog.campo;

/**
 * Registra que um consumidor foi cadastrado: o evento {@code consumidor.criado}.
 *
 * <p>
 * Leva só o id: o documento, o contato e o endereço são dado pessoal, e o id basta para achar o
 * consumidor.
 */
public final class ConsumidorCriado {

    private ConsumidorCriado() {
    }

    /**
     * Registra o cadastro.
     *
     * @param saida      a porta de log
     * @param consumidor o consumidor gravado, com id
     */
    public static void registrar(SaidaDeLogPorta saida, Consumidor consumidor) {
        saida.informacao("consumidor.criado", campo("consumidorId", consumidor.id().valor()));
    }
}
