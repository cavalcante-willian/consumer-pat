package br.com.alelo.consumer.consumerpat.dominio.cartao;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroNumeroDeCartaoInvalido;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Lê um número de cartão de texto vindo de fora, sem lançar exceção.
 *
 * <p>
 * É a porta de entrada do número externo, e é aqui que ele ganha a forma canônica: os espaços e os
 * hífens de {@code 5555 0000 1111 2222} saem, porque o mesmo cartão escrito de duas maneiras
 * precisa ser o mesmo número, ou uma restrição de unicidade do banco deixaria passar duplicatas.
 * Qualquer outro caractere, como uma letra ou um ponto, recusa o texto em vez de ser descartado em
 * silêncio, porque descartá-lo mudaria o número que a pessoa digitou.
 */
public final class NumeroDeCartaoDeTexto {

    private static final Pattern SEPARADORES = Pattern.compile("[ -]");

    private static final Pattern FORMATO = Pattern.compile("\\d{" + NumeroDeCartao.MINIMO_DE_DIGITOS
            + "," + NumeroDeCartao.MAXIMO_DE_DIGITOS + "}");

    private NumeroDeCartaoDeTexto() {
    }

    /**
     * Converte o texto num número de cartão.
     *
     * @param texto o número, com ou sem espaços e hífens, como "5555 0000 1111 2222"
     * @return sucesso com o número na forma canônica, ou falha com
     *         {@link ErroNumeroDeCartaoInvalido}, que não repete o texto recebido
     */
    public static Resultado<NumeroDeCartao, ErroDominio> converter(String texto) {
        return AusenciaComoErro.converter(
                Optional.ofNullable(texto)
                        .map(candidato -> SEPARADORES.matcher(candidato).replaceAll(""))
                        .filter(FORMATO.asMatchPredicate()).map(NumeroDeCartao::new),
                ErroNumeroDeCartaoInvalido::new);
    }
}
