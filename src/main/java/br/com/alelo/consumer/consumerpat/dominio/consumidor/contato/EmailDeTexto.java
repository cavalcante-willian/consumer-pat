package br.com.alelo.consumer.consumerpat.dominio.consumidor.contato;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroEmailInvalido;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

import java.util.Optional;

/**
 * Lê um e-mail escrito como texto, vindo de fora do domínio.
 *
 * <p>
 * Nunca lança, e a falha não repete o texto recebido.
 */
public final class EmailDeTexto {

    private EmailDeTexto() {
    }

    /**
     * Converte o texto em e-mail, ou devolve o erro de validação.
     *
     * @param texto o e-mail como o cliente o escreveu, possivelmente nulo
     * @return o e-mail, ou {@link ErroEmailInvalido}
     */
    public static Resultado<Email, ErroDominio> converter(String texto) {
        return AusenciaComoErro.converter(
                Optional.ofNullable(texto).map(String::strip)
                        .filter(Email.FORMATO.asMatchPredicate()).map(Email::new),
                ErroEmailInvalido::new);
    }
}
