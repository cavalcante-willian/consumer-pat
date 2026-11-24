package br.com.alelo.consumer.consumerpat.api.comum.problema;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * O corpo de erro da API, no formato de problem details (RFC 7807), com o tipo de mídia
 * {@code application/problem+json}.
 *
 * <p>
 * É o único formato de erro: nunca o JSON padrão do Spring, nunca uma página de erro, nunca um
 * texto inventado por endpoint. Os campos vazios não saem no JSON.
 *
 * @param type     URN estável com o código do erro, que é o que o cliente compara; nunca varia
 * @param title    título humano, que pode mudar sem nova versão
 * @param status   o código HTTP da resposta
 * @param detail   o caso concreto, sem detalhe interno e sem dado sensível
 * @param instance o caminho da requisição que falhou
 * @param errors   os campos da entrada com erro e as mensagens de cada um; vazio se o erro não é de
 *                 campo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record MensagemProblema(String type, String title, int status, String detail,
        String instance, Map<String, List<String>> errors) {
}
