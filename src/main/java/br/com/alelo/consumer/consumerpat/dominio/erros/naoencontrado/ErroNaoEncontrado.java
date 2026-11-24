package br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado;

import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;

/**
 * Categoria dos erros em que o recurso nomeado pela entrada não existe.
 *
 * <p>
 * A categoria é dada uma vez aqui: cada variante herda {@link #categoria()} e só informa o que é
 * próprio dela, o código e a mensagem. Como a interface é selada, a lista de variantes é fechada e
 * o compilador, e o teste de códigos, as conhecem.
 */
public sealed interface ErroNaoEncontrado extends ErroDominio
        permits ErroCartaoNaoEncontrado, ErroConsumidorNaoEncontrado {

    @Override
    default CategoriaErro categoria() {
        return CategoriaErro.NAO_ENCONTRADO;
    }
}
