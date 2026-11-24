package br.com.alelo.consumer.consumerpat.dominio.erros.conflito;

import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;

/**
 * Erro de conflito: o pedido é válido, mas colide com o que já existe ou com uma escrita que
 * aconteceu antes dele.
 *
 * <p>
 * É diferente da regra de negócio, que recusa pelo estado do domínio (o saldo não cobre), e da
 * validação, que recusa a forma do dado: aqui a forma está certa, e quem pediu pode ter razão de
 * tentar de novo depois de reler o estado. A categoria é dada uma vez, e cada variante só informa o
 * código e a mensagem.
 */
public sealed interface ErroConflito extends ErroDominio
        permits ErroDocumentoJaCadastrado, ErroNumeroDeCartaoJaCadastrado, ErroVersaoDivergente {

    @Override
    default CategoriaErro categoria() {
        return CategoriaErro.CONFLITO;
    }
}
