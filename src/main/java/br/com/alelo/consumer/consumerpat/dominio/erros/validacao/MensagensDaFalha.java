package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;

import java.util.List;
import java.util.Optional;

/**
 * As mensagens de um erro, para quem as reúne com as de outros.
 *
 * <p>
 * Um erro que aponta campos traz as mensagens deles, e um que não aponta nenhum traz a própria
 * mensagem, de modo que nenhuma falha chega à resposta sem texto.
 */
public final class MensagensDaFalha {

    private MensagensDaFalha() {
    }

    /**
     * Extrai as mensagens do erro.
     *
     * @param erro o erro de domínio
     * @return as mensagens por campo, achatadas, ou a mensagem do erro se ele não aponta campo
     */
    public static List<String> de(ErroDominio erro) {
        var dosCampos = erro.camposComErro().values().stream().flatMap(List::stream).toList();
        return Optional.of(dosCampos).filter(mensagens -> !mensagens.isEmpty())
                .orElseGet(() -> List.of(erro.mensagem()));
    }
}
