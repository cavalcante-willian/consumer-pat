package br.com.alelo.consumer.consumerpat.dominio.pagina;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroDirecaoInvalida;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

import java.util.Arrays;

/**
 * Lê a direção pedida pelo nome, sem diferenciar maiúsculas de minúsculas.
 */
public final class DirecaoDeOrdenacaoDeTexto {

    private DirecaoDeOrdenacaoDeTexto() {
    }

    /**
     * Converte o texto em direção.
     *
     * @param texto o valor recebido, possivelmente nulo
     * @return a direção, ou {@link ErroDirecaoInvalida}
     */
    public static Resultado<DirecaoDeOrdenacao, ErroDominio> converter(String texto) {
        return AusenciaComoErro.converter(
                Arrays.stream(DirecaoDeOrdenacao.values())
                        .filter(direcao -> direcao.name().equalsIgnoreCase(texto)).findFirst(),
                ErroDirecaoInvalida::new);
    }
}
