package br.com.alelo.consumer.consumerpat.dominio.consumidor.documento;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroDocumentoInvalido;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Lê um documento escrito como texto, vindo de fora do domínio.
 *
 * <p>
 * Aceita a pontuação comum ({@code .}, {@code -}, {@code /} e espaço), que remove, e exige que
 * sobrem 11 ou 14 dígitos. Qualquer outro caractere recusa o texto. Nunca lança, e a falha não
 * repete o texto recebido, que seria um documento.
 */
public final class DocumentoDeTexto {

    private static final Pattern PONTUACAO = Pattern.compile("[.\\-/ ]");

    private static final Pattern FORMATO = Pattern.compile(
            "\\d{" + Documento.DIGITOS_DE_CPF + "}|\\d{" + Documento.DIGITOS_DE_CNPJ + "}");

    private DocumentoDeTexto() {
    }

    /**
     * Converte o texto em documento, ou devolve o erro de validação.
     *
     * @param texto o documento como o cliente o escreveu, possivelmente nulo
     * @return o documento na forma canônica, ou {@link ErroDocumentoInvalido}
     */
    public static Resultado<Documento, ErroDominio> converter(String texto) {
        return AusenciaComoErro.converter(
                Optional.ofNullable(texto)
                        .map(candidato -> PONTUACAO.matcher(candidato).replaceAll(""))
                        .filter(FORMATO.asMatchPredicate()).map(Documento::new),
                ErroDocumentoInvalido::new);
    }
}
