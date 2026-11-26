package br.com.alelo.consumer.consumerpat.medicao;

/**
 * Da resposta, só o que a medição guarda: o status e o tamanho.
 *
 * <p>
 * O corpo da listagem do legado tem dezenas de megabytes, e uma medição guarda uma resposta por
 * execução. Guardar o corpo inteiro de todas custaria memória do próprio driver, e entortaria o que
 * se está medindo.
 *
 * @param status o status HTTP
 * @param bytes  o tamanho do corpo
 */
public record ResumoDaResposta(int status, int bytes) {

    /**
     * Resume uma resposta.
     *
     * @param resposta a resposta
     * @return o status e o tamanho dela
     */
    public static ResumoDaResposta de(RespostaHttp resposta) {
        return new ResumoDaResposta(resposta.status(), resposta.tamanho());
    }
}
