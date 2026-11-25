package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import br.com.alelo.consumer.consumerpat.dominio.pagina.OrdenacaoDeConsumidor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A ordenação pedida não é um dos campos que a listagem aceita.
 *
 * <p>
 * Não carrega o texto recebido, e a mensagem lista os valores aceitos. O cliente nunca escolhe uma
 * coluna: escolhe um destes valores.
 */
public record ErroOrdenacaoInvalida() implements ErroValidacao {

    /** Código estável desta variante. */
    public static final String CODIGO = "CPT-VAL-PAG-003";

    /** Os valores aceitos, separados por vírgula, na ordem do enum. */
    public static final String VALORES_ACEITOS = Arrays.stream(OrdenacaoDeConsumidor.values())
            .map(Enum::name).collect(Collectors.joining(", "));

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String mensagem() {
        return "Ordenação inválida: use " + VALORES_ACEITOS + ".";
    }

    @Override
    public Map<String, List<String>> camposComErro() {
        return Map.of("ordenacao", List.of("deve ser " + VALORES_ACEITOS));
    }
}
