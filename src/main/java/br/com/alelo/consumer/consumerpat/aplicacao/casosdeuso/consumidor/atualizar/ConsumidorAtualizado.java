package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.atualizar;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.log.SaidaDeLogPorta;

import static br.com.alelo.consumer.consumerpat.dominio.log.CampoDeLog.campo;

/**
 * Registra que o cadastro de um consumidor foi atualizado: o evento {@code consumidor.atualizado}.
 *
 * <p>
 * Leva o id e a versão como ficou, que é a que o cliente precisa reenviar na próxima atualização, e
 * nada do cadastro: o documento, o contato e o endereço são dado pessoal.
 */
public final class ConsumidorAtualizado {

    private ConsumidorAtualizado() {
    }

    /**
     * Registra a atualização.
     *
     * @param saida      a porta de log
     * @param consumidor o consumidor como ficou gravado, com a versão nova
     */
    public static void registrar(SaidaDeLogPorta saida, Consumidor consumidor) {
        saida.informacao("consumidor.atualizado", campo("consumidorId", consumidor.id().valor()),
                campo("versao", consumidor.versao()));
    }
}
