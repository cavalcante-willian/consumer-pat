package br.com.alelo.consumer.consumerpat.api.comum.requisicao;

import org.slf4j.MDC;

/**
 * Lê o id da requisição que a thread atual está atendendo.
 *
 * <p>
 * É o único lugar, fora do filtro de correlação, que toca o {@code MDC}: a única exceção declarada
 * à regra de que só {@code infraestrutura.log} importa o SLF4J. O corpo de erro o usa para dizer ao
 * usuário qual id reportar, e o operador o encontra em toda linha do log.
 */
public final class IdDaRequisicaoAtual {

    private IdDaRequisicaoAtual() {
    }

    /**
     * Devolve o id.
     *
     * @return o id da requisição, ou nulo fora de uma requisição
     */
    public static String ler() {
        return MDC.get(IdDeRequisicao.CHAVE_NO_MDC);
    }
}
