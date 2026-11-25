package br.com.alelo.consumer.consumerpat.api.consumidor.atualizar;

import br.com.alelo.consumer.consumerpat.api.consumidor.DadosDaMensagem;
import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoAtualizacaoDeConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorIdDeNumero;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

/**
 * Converte o id da rota e o corpo da atualização no pedido do caso de uso.
 *
 * <p>
 * O id é lido primeiro, e um id que não é positivo para a conversão: sem id não há o que atualizar.
 * Os dados, que são independentes entre si, saem com todos os erros de uma vez.
 */
public final class AtualizacaoDaMensagem {

    private AtualizacaoDaMensagem() {
    }

    /**
     * Converte o pedido.
     *
     * @param id       o id da rota
     * @param mensagem o corpo da atualização
     * @return o pedido do caso de uso, ou um erro de validação
     */
    public static Resultado<DtoAtualizacaoDeConsumidor, ErroDominio> converter(long id,
            MensagemAtualizarConsumidor mensagem) {
        return ConsumidorIdDeNumero.converter(id)
                .encadear(consumidorId -> DadosDaMensagem.converter(mensagem.dados())
                        .mapear(dados -> new DtoAtualizacaoDeConsumidor(consumidorId,
                                mensagem.versao(), dados)));
    }
}
