package br.com.alelo.consumer.consumerpat.dominio.consumidor.endereco;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroCepInvalido;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Lê um CEP escrito como texto, vindo de fora do domínio.
 *
 * <p>
 * Aceita o hífen, que remove, e exige 8 dígitos. Nunca lança.
 */
public final class CepDeTexto {

    private static final Pattern PONTUACAO = Pattern.compile("[.\\-]");

    private static final Pattern FORMATO = Pattern.compile("\\d{" + Cep.DIGITOS + "}");

    private CepDeTexto() {
    }

    /**
     * Converte o texto em CEP, ou devolve o erro de validação.
     *
     * @param texto o CEP como o cliente o escreveu, possivelmente nulo
     * @return o CEP na forma canônica, ou {@link ErroCepInvalido}
     */
    public static Resultado<Cep, ErroDominio> converter(String texto) {
        return AusenciaComoErro.converter(Optional.ofNullable(texto)
                .map(candidato -> PONTUACAO.matcher(candidato).replaceAll(""))
                .filter(FORMATO.asMatchPredicate()).map(Cep::new), ErroCepInvalido::new);
    }
}
