package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;

/**
 * Categoria dos erros em que a entrada está malformada.
 *
 * <p>
 * A categoria é dada uma vez aqui: cada variante herda {@link #categoria()} e só informa o que é
 * próprio dela, o código e a mensagem. Como a interface é selada, a lista de variantes é fechada e
 * o compilador, e o teste de códigos, as conhecem.
 */
public sealed interface ErroValidacao extends ErroDominio permits ErroNumeroDeCartaoInvalido,
        ErroTipoEstabelecimentoInvalido, ErroValorMonetarioInvalido {

    @Override
    default CategoriaErro categoria() {
        return CategoriaErro.VALIDACAO;
    }
}
