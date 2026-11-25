package br.com.alelo.consumer.consumerpat.infraestrutura.log;

import java.util.regex.Pattern;

/**
 * Garante que um texto vira uma linha só do log.
 *
 * <p>
 * Um valor com quebra de linha forjaria uma linha de log inteira, com data, nível e evento
 * inventados. Cada quebra vira um sublinhado, e o resto do texto não muda.
 */
final class TextoDeUmaLinha {

    private static final Pattern QUEBRAS = Pattern.compile("[\\r\\n\\u2028\\u2029]");

    private TextoDeUmaLinha() {
    }

    /**
     * Troca as quebras de linha por sublinhado.
     *
     * @param texto o texto original
     * @return o texto sem quebras de linha
     */
    static String de(String texto) {
        return QUEBRAS.matcher(texto).replaceAll("_");
    }
}
