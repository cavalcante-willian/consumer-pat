package br.com.alelo.consumer.consumerpat.seguranca;

import java.util.Map;

/**
 * O que uma verificação concluiu sobre um sistema.
 *
 * @param chave     o nome estável da verificação, igual nos dois sistemas, pelo qual o relatório
 *                  junta o legado e o novo na mesma linha
 * @param descricao o que a verificação tenta
 * @param veredito  protegido, exposto, ou não verificado quando o ambiente não permitiu verificar
 * @param evidencia o que foi observado: o status, o efeito no saldo, o que a resposta continha, ou
 *                  o motivo de não ter verificado
 */
public record Achado(String chave, String descricao, Veredito veredito, String evidencia) {

    private static final Map<Boolean, Veredito> DE_PROTEGIDO = Map.of(true, Veredito.PROTEGIDO,
            false, Veredito.EXPOSTO);

    /**
     * Um achado de uma verificação que foi feita: o sistema se protegeu ou se expôs.
     *
     * @param chave     o nome estável da verificação
     * @param descricao o que a verificação tenta
     * @param protegido {@code true} se o sistema se defendeu, {@code false} se deixou passar
     * @param evidencia o que foi observado
     */
    public Achado(String chave, String descricao, boolean protegido, String evidencia) {
        this(chave, descricao, DE_PROTEGIDO.get(protegido), evidencia);
    }

    /**
     * Um achado de uma verificação que o ambiente não deixou fazer.
     *
     * @param chave     o nome estável da verificação
     * @param descricao o que a verificação tenta
     * @param motivo    por que não foi possível verificar
     * @return o achado, não verificado
     */
    public static Achado naoVerificado(String chave, String descricao, String motivo) {
        return new Achado(chave, descricao, Veredito.NAO_VERIFICADO, motivo);
    }

    /**
     * Diz se o sistema se defendeu.
     *
     * @return {@code true} só se o veredito é protegido: um achado não verificado não é protegido
     */
    public boolean protegido() {
        return veredito == Veredito.PROTEGIDO;
    }
}
