package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import java.util.List;
import java.util.Map;

/**
 * O texto recebido não é um número de cartão: não tem de 13 a 19 dígitos, depois de retirados os
 * espaços e os hífens.
 *
 * <p>
 * Não carrega o texto recebido, de propósito. Um texto recusado como número de cartão ainda pode
 * ser um número de cartão quase certo, e devolvê-lo na mensagem, ou gravá-lo em um log, vazaria o
 * dado que o tipo existe para proteger. A mensagem diz só o formato esperado, o que basta para
 * corrigir. Aponta o campo {@code numeroCartao}.
 */
public record ErroNumeroDeCartaoInvalido() implements ErroValidacao {

    /** Código estável desta variante. */
    public static final String CODIGO = "CPT-VAL-CRT-002";

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String mensagem() {
        return "Número de cartão inválido: informe de 13 a 19 dígitos.";
    }

    @Override
    public Map<String, List<String>> camposComErro() {
        return Map.of("numeroCartao", List.of("deve ter de 13 a 19 dígitos"));
    }
}
