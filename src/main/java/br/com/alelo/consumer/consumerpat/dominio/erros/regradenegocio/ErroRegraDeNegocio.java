package br.com.alelo.consumer.consumerpat.dominio.erros.regradenegocio;

import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;

/**
 * Categoria dos erros em que a entrada é válida e uma regra de negócio a recusa.
 *
 * <p>
 * A categoria é dada uma vez aqui: cada variante herda {@link #categoria()} e só informa o que é
 * próprio dela, o código e a mensagem. Como a interface é selada, a lista de variantes é fechada e
 * o compilador, e o teste de códigos, as conhecem.
 */
public sealed interface ErroRegraDeNegocio extends ErroDominio permits ErroSaldoInsuficiente {

    @Override
    default CategoriaErro categoria() {
        return CategoriaErro.REGRA_DE_NEGOCIO;
    }
}
