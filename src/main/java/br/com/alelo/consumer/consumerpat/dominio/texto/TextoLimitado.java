package br.com.alelo.consumer.consumerpat.dominio.texto;

import java.util.Optional;

/**
 * Exige um texto não vazio, de tamanho limitado, sem espaço nas pontas.
 *
 * <p>
 * O limite é o da coluna que vai guardar o texto, de modo que um texto grande demais é recusado na
 * construção do tipo de domínio, e não estoura na gravação nem é truncado em silêncio.
 */
public final class TextoLimitado {

    private TextoLimitado() {
    }

    /**
     * Devolve o texto sem espaço nas pontas, se ele tem de 1 até o máximo de caracteres.
     *
     * @param valor  o texto recebido
     * @param nome   o nome do dado, com artigo ("O nome"), para a mensagem da exceção
     * @param maximo o maior tamanho aceito, depois de tirar o espaço das pontas
     * @return o texto sem espaço nas pontas
     * @throws IllegalArgumentException se o texto é nulo, vazio ou passa do limite; a mensagem não
     *                                  traz o valor
     */
    public static String exigir(String valor, String nome, int maximo) {
        return Optional.ofNullable(valor).map(String::strip)
                .filter(candidato -> !candidato.isEmpty() && candidato.length() <= maximo)
                .orElseThrow(() -> new IllegalArgumentException(
                        nome + " precisa ter de 1 a " + maximo + " caracteres"));
    }
}
