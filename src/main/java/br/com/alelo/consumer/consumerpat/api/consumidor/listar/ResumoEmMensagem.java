package br.com.alelo.consumer.consumerpat.api.consumidor.listar;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.ResumoDeConsumidor;

/**
 * Converte o resumo do domínio na mensagem de saída da listagem.
 */
public final class ResumoEmMensagem {

    private ResumoEmMensagem() {
    }

    /**
     * Monta a mensagem.
     *
     * @param resumo o resumo do domínio
     * @return o id e o nome
     */
    public static MensagemResumoConsumidor montar(ResumoDeConsumidor resumo) {
        return new MensagemResumoConsumidor(resumo.id().valor(), resumo.nome());
    }
}
