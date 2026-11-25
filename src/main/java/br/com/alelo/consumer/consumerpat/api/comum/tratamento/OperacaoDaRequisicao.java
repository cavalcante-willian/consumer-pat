package br.com.alelo.consumer.consumerpat.api.comum.tratamento;

import javax.servlet.http.HttpServletRequest;

/**
 * Diz qual operação a requisição pedia, para o campo {@code operacao} do log de falha.
 *
 * <p>
 * É o método e o caminho, sem a query string, que pode carregar dado do usuário.
 */
public final class OperacaoDaRequisicao {

    private OperacaoDaRequisicao() {
    }

    /**
     * Descreve a operação.
     *
     * @param requisicao a requisição
     * @return o método e o caminho, como {@code POST /api/v1/compras}
     */
    public static String de(HttpServletRequest requisicao) {
        return requisicao.getMethod() + " " + requisicao.getRequestURI();
    }
}
