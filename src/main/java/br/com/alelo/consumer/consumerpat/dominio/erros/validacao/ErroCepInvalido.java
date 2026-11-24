package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import java.util.List;
import java.util.Map;

/**
 * O CEP informado não tem 8 dígitos.
 *
 * <p>
 * Não carrega o texto recebido.
 */
public record ErroCepInvalido() implements ErroValidacao {

    /** Código estável desta variante. */
    public static final String CODIGO = "CPT-VAL-CON-004";

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String mensagem() {
        return "CEP inválido: informe 8 dígitos.";
    }

    @Override
    public Map<String, List<String>> camposComErro() {
        return Map.of("cep", List.of("deve ter 8 dígitos"));
    }
}
