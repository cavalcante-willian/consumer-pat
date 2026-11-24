package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import java.util.List;
import java.util.Map;

/**
 * O e-mail informado não tem o formato {@code nome@dominio.sufixo} ou passa de 254 caracteres.
 *
 * <p>
 * Não carrega o texto recebido.
 */
public record ErroEmailInvalido() implements ErroValidacao {

    /** Código estável desta variante. */
    public static final String CODIGO = "CPT-VAL-CON-003";

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String mensagem() {
        return "E-mail inválido: use o formato nome@dominio.sufixo.";
    }

    @Override
    public Map<String, List<String>> camposComErro() {
        return Map.of("email", List.of("deve ter o formato nome@dominio.sufixo"));
    }
}
