package br.com.alelo.consumer.consumerpat.fabricas;

import com.fasterxml.jackson.core.io.JsonStringEncoder;

/**
 * Escapa um texto para ir dentro de uma string JSON.
 *
 * <p>
 * Os corpos dos testes são montados como texto, e um valor hostil, com aspas e barras, quebraria o
 * JSON e faria o teste exercitar o analisador, e não o campo que ele queria exercitar.
 */
public final class JsonDeTeste {

    private JsonDeTeste() {
    }

    /**
     * O conteúdo de uma string JSON, sem as aspas das pontas.
     *
     * @param texto o texto, que pode ter aspas, barras e caracteres de controle
     * @return o texto escapado
     */
    public static String escapar(String texto) {
        return new String(JsonStringEncoder.getInstance().quoteAsString(texto));
    }
}
