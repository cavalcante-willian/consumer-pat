package br.com.alelo.consumer.consumerpat.seguranca;

/**
 * A conclusão de uma verificação.
 *
 * <p>
 * Há três, e não duas: uma verificação que não pôde ser feita não se protegeu nem se expôs.
 * Contá-la como exposta acusaria o sistema de uma falha que ninguém viu, e contá-la como protegida
 * o absolveria sem prova. O que ela diz é que não verificou, e por quê.
 */
public enum Veredito {

    /** O sistema se defendeu. */
    PROTEGIDO("PROTEGIDO"),

    /** O sistema deixou passar. */
    EXPOSTO("EXPOSTO  "),

    /** A verificação não pôde ser feita neste ambiente. */
    NAO_VERIFICADO("NÃO VERIFICADO");

    private final String rotulo;

    Veredito(String rotulo) {
        this.rotulo = rotulo;
    }

    /**
     * Como o relatório escreve o veredito.
     *
     * @return o rótulo
     */
    public String rotulo() {
        return rotulo;
    }
}
