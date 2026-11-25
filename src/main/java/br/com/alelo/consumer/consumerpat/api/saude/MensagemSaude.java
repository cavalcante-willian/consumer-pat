package br.com.alelo.consumer.consumerpat.api.saude;

import java.util.Map;

/**
 * O corpo do health: a situação geral e o estado de cada dependência.
 *
 * <p>
 * É público e sem autenticação, então expõe o mínimo: nem URL de conexão, nem host, nem versão do
 * banco, nem mensagem de exceção.
 *
 * @param estado       a situação geral, {@code SAUDAVEL} ou {@code DEGRADADO}
 * @param dependencias o estado de cada dependência, pelo nome dela
 */
public record MensagemSaude(String estado, Map<String, String> dependencias) {
}
