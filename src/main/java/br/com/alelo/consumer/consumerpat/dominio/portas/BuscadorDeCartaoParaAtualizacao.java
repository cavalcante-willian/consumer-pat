package br.com.alelo.consumer.consumerpat.dominio.portas;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado.ErroCartaoNaoEncontrado;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

/**
 * Papel de quem sabe buscar um cartão para alterá-lo.
 *
 * <p>
 * É a leitura que precede um débito ou um crédito. O nome diz o contrato: o cartão devolvido é
 * mantido travado até o fim da transação em curso, de modo que dois débitos simultâneos sobre o
 * mesmo cartão se enfileiram em vez de sobrescreverem um ao outro. Quem só quer ler o saldo não usa
 * esta operação.
 */
public interface BuscadorDeCartaoParaAtualizacao {

    /**
     * Busca o cartão do número e do tipo informados e o trava para atualização.
     *
     * @param numero o número do cartão
     * @param tipo   o tipo esperado; um cartão de outro tipo com o mesmo número <b>não</b> é
     *               devolvido
     * @return sucesso com o cartão travado, ou falha com {@link ErroCartaoNaoEncontrado} quando não
     *         existe cartão com esse número e tipo; ausência é retorno, e não exceção
     */
    Resultado<Cartao, ErroDominio> buscarPorNumeroETipoParaAtualizacao(NumeroDeCartao numero,
            TipoCartao tipo);
}
