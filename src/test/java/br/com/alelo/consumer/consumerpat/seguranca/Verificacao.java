package br.com.alelo.consumer.consumerpat.seguranca;

/**
 * Uma tentativa de ataque ou de abuso, feita da mesma forma contra qualquer sistema.
 *
 * <p>
 * A verificação só pede à {@link SondaDeSeguranca}, e por isso o mesmo código a faz contra o legado
 * e contra o novo. Ela conclui, e não afirma: devolver um {@link Achado} de exposição é um
 * resultado, e o que faz um teste falhar é decidido por quem usa a bateria.
 */
public interface Verificacao {

    /**
     * O nome estável da verificação.
     *
     * @return a chave, igual nos dois sistemas
     */
    String chave();

    /**
     * Tenta e conclui.
     *
     * @param sonda o sistema a verificar
     * @return o que foi observado
     * @throws Exception se uma chamada falha por motivo que não é a resposta do sistema
     */
    Achado verificar(SondaDeSeguranca sonda) throws Exception;
}
