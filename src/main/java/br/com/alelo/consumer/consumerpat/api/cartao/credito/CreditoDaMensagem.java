package br.com.alelo.consumer.consumerpat.api.cartao.credito;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoCredito;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartaoDeTexto;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartaoDoEstabelecimento;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

/**
 * Converte o corpo do pedido de crédito no pedido do caso de uso.
 *
 * <p>
 * É onde o tipo e o número da fronteira viram os tipos do domínio, com as leituras que ele tem. Uma
 * conversão que falha devolve o erro de validação do próprio domínio, e a resposta é a mesma do
 * Bean Validation: o cliente não distingue quem recusou. Só o primeiro erro sai.
 */
public final class CreditoDaMensagem {

    private CreditoDaMensagem() {
    }

    /**
     * Converte a mensagem.
     *
     * @param mensagem o corpo, já validado na borda
     * @return o pedido com os tipos do domínio, ou o primeiro erro de conversão
     */
    public static Resultado<DtoCredito, ErroDominio> converter(MensagemCreditarCartao mensagem) {
        return TipoCartaoDoEstabelecimento.converter(mensagem.tipoEstabelecimento())
                .encadear(tipo -> NumeroDeCartaoDeTexto.converter(mensagem.numeroCartao()).mapear(
                        numero -> new DtoCredito(numero, tipo, new Dinheiro(mensagem.valor()))));
    }
}
