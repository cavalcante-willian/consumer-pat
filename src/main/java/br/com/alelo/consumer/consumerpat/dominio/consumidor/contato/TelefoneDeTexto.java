package br.com.alelo.consumer.consumerpat.dominio.consumidor.contato;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroTelefoneInvalido;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Lê um telefone escrito como texto, vindo de fora do domínio.
 *
 * <p>
 * Aceita a pontuação comum ({@code ( ) + - .} e espaço), que remove, e exige de 10 a 15 dígitos.
 * Nunca lança, e a falha aponta o campo, e não o texto recebido.
 */
public final class TelefoneDeTexto {

    private static final Pattern PONTUACAO = Pattern.compile("[ ()+.\\-]");

    private static final Pattern FORMATO = Pattern
            .compile("\\d{" + Telefone.MINIMO_DE_DIGITOS + "," + Telefone.MAXIMO_DE_DIGITOS + "}");

    private TelefoneDeTexto() {
    }

    /**
     * Converte o texto em telefone, ou devolve o erro de validação do campo.
     *
     * @param texto o telefone como o cliente o escreveu, possivelmente nulo
     * @param campo o nome do campo da entrada, para o erro apontá-lo
     * @return o telefone na forma canônica, ou {@link ErroTelefoneInvalido}
     */
    public static Resultado<Telefone, ErroDominio> converter(String texto, String campo) {
        return AusenciaComoErro.converter(
                Optional.ofNullable(texto)
                        .map(candidato -> PONTUACAO.matcher(candidato).replaceAll(""))
                        .filter(FORMATO.asMatchPredicate()).map(Telefone::new),
                () -> new ErroTelefoneInvalido(campo));
    }
}
