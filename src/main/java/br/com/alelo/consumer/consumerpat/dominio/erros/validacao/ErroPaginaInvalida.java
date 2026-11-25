package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import java.util.List;
import java.util.Map;

/**
 * A página pedida não é um número a partir de 1.
 *
 * <p>
 * O contrato da API começa a página em 1, e não em 0 como o banco: zero e negativos são recusados.
 */
public record ErroPaginaInvalida() implements ErroValidacao {

    /** Código estável desta variante. */
    public static final String CODIGO = "CPT-VAL-PAG-001";

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String mensagem() {
        return "Página inválida: a primeira página é a 1.";
    }

    @Override
    public Map<String, List<String>> camposComErro() {
        return Map.of("pagina", List.of("deve ser 1 ou maior"));
    }
}
