package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import java.util.List;
import java.util.Map;

/**
 * O id de consumidor informado não é um número positivo.
 *
 * <p>
 * Só um consumidor que já existe tem id, e o primeiro é 1. Um id zero ou negativo nunca existe, e é
 * recusado na leitura em vez de lançar exceção ao construir o tipo.
 */
public record ErroConsumidorIdInvalido() implements ErroValidacao {

    /** Código estável desta variante. */
    public static final String CODIGO = "CPT-VAL-CON-005";

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String mensagem() {
        return "Id de consumidor inválido: use um número maior que zero.";
    }

    @Override
    public Map<String, List<String>> camposComErro() {
        return Map.of("id", List.of("deve ser maior que zero"));
    }
}
