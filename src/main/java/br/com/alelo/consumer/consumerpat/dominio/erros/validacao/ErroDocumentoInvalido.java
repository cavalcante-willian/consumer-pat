package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import java.util.List;
import java.util.Map;

/**
 * O documento informado não tem 11 nem 14 dígitos.
 *
 * <p>
 * Não carrega o texto recebido: ele seria um documento, e erro de validação vai para o log e para a
 * resposta.
 */
public record ErroDocumentoInvalido() implements ErroValidacao {

    /** Código estável desta variante. */
    public static final String CODIGO = "CPT-VAL-CON-001";

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String mensagem() {
        return "Documento inválido: informe 11 dígitos (CPF) ou 14 (CNPJ).";
    }

    @Override
    public Map<String, List<String>> camposComErro() {
        return Map.of("documento", List.of("deve ter 11 ou 14 dígitos"));
    }
}
