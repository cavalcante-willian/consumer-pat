package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import br.com.alelo.consumer.consumerpat.dominio.pagina.DirecaoDeOrdenacao;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A direção pedida não é uma das que a ordenação aceita.
 *
 * <p>
 * Não carrega o texto recebido, e a mensagem lista os valores aceitos.
 */
public record ErroDirecaoInvalida() implements ErroValidacao {

    /** Código estável desta variante. */
    public static final String CODIGO = "CPT-VAL-PAG-004";

    /** Os valores aceitos, separados por vírgula, na ordem do enum. */
    public static final String VALORES_ACEITOS = Arrays.stream(DirecaoDeOrdenacao.values())
            .map(Enum::name).collect(Collectors.joining(", "));

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String mensagem() {
        return "Direção inválida: use " + VALORES_ACEITOS + ".";
    }

    @Override
    public Map<String, List<String>> camposComErro() {
        return Map.of("direcao", List.of("deve ser " + VALORES_ACEITOS));
    }
}
