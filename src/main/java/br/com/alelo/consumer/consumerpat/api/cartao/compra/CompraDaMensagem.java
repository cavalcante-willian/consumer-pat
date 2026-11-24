package br.com.alelo.consumer.consumerpat.api.cartao.compra;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoCompra;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartaoDeTexto;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartaoDoEstabelecimento;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

/**
 * Converte o corpo do pedido de compra no pedido do caso de uso.
 *
 * <p>
 * É onde o texto e os números da fronteira viram os tipos do domínio, com as leituras que ele tem.
 * Uma conversão que falha, como um tipo de estabelecimento que não existe, devolve o erro de
 * validação do próprio domínio, e a resposta é a mesma do Bean Validation: o cliente não distingue
 * quem recusou. Só o primeiro erro sai, porque cada leitura é uma conversão, e não um objeto
 * inteiro.
 */
public final class CompraDaMensagem {

    private CompraDaMensagem() {
    }

    /**
     * Converte a mensagem.
     *
     * @param mensagem o corpo, já validado na borda
     * @return o pedido com os tipos do domínio, ou o primeiro erro de conversão
     */
    public static Resultado<DtoCompra, ErroDominio> converter(MensagemRegistrarCompra mensagem) {
        return TipoCartaoDoEstabelecimento.converter(mensagem.tipoEstabelecimento())
                .encadear(tipo -> NumeroDeCartaoDeTexto.converter(mensagem.numeroCartao())
                        .mapear(numero -> new DtoCompra(tipo, mensagem.nomeEstabelecimento(),
                                numero, mensagem.descricaoProduto(),
                                new Dinheiro(mensagem.valor()))));
    }
}
