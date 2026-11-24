package br.com.alelo.consumer.consumerpat.api.comum.campos;

import java.util.List;
import java.util.Map;

/**
 * Como uma causa que o Jackson aponta vira os campos com erro da resposta.
 *
 * <p>
 * É o comportamento que o registro de {@link TradutoresDoCorpo} guarda para cada tipo de exceção.
 * Existe para dar nome ao conceito, e não repeti-lo como um tipo genérico de três níveis a cada
 * uso.
 */
@FunctionalInterface
public interface TradutorDeCausa {

    /**
     * Traduz a causa.
     *
     * @param causa a exceção que o registro associou a este tradutor
     * @return o campo com erro e a mensagem dele
     */
    Map<String, List<String>> traduzir(Throwable causa);
}
