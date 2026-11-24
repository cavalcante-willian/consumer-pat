package br.com.alelo.consumer.consumerpat.dominio.dinheiro;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroValorMonetarioInvalido;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Lê uma quantia em dinheiro de texto vindo de fora, sem lançar exceção.
 *
 * <p>
 * É a única porta de entrada de dinheiro externo, e o motivo de existir à parte do {@link Dinheiro}
 * é que o formato aceito muda por razões próprias (a precisão da coluna, uma nova regra de entrada)
 * sem que a aritmética mude.
 */
public final class DinheiroDeTexto {

    /** Número com ponto, de até 13 dígitos inteiros e 6 casas, e sinal opcional. */
    private static final Pattern FORMATO = Pattern.compile("-?\\d{1,"
            + (Dinheiro.PRECISAO - Dinheiro.ESCALA) + "}(\\.\\d{1," + Dinheiro.ESCALA + "})?");

    private DinheiroDeTexto() {
    }

    /**
     * Converte o texto numa quantia.
     *
     * <p>
     * Aceita um número com ponto, de até 13 dígitos inteiros e 6 casas, com sinal opcional. Recusa
     * texto nulo, vazio, com vírgula, com notação científica ou com casas a mais.
     *
     * @param texto a quantia em texto, como "10.50"
     * @return sucesso com a quantia, ou falha com {@link ErroValorMonetarioInvalido}
     */
    public static Resultado<Dinheiro, ErroDominio> converter(String texto) {
        return AusenciaComoErro.converter(
                Optional.ofNullable(texto).filter(candidato -> FORMATO.matcher(candidato).matches())
                        .map(BigDecimal::new).map(Dinheiro::new),
                ErroValorMonetarioInvalido::new);
    }
}
