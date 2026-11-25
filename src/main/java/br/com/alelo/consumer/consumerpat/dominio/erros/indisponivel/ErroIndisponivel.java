package br.com.alelo.consumer.consumerpat.dominio.erros.indisponivel;

import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;

/**
 * Erro de indisponibilidade: uma dependência crítica não responde.
 *
 * <p>
 * É diferente do conflito, em que o mundo mudou e reler resolve, e da regra de negócio, em que o
 * estado do domínio recusa: aqui o pedido pode estar certo, e quem o fez nada errou. A categoria é
 * dada uma vez, e cada variante só informa o código e a mensagem.
 */
public sealed interface ErroIndisponivel extends ErroDominio permits ErroBancoIndisponivel {

    @Override
    default CategoriaErro categoria() {
        return CategoriaErro.INDISPONIVEL;
    }
}
