package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * O nome informado não é o de nenhum tipo de cartão.
 *
 * <p>
 * É diferente do código de estabelecimento, que identifica onde uma compra foi feita: aqui é o tipo
 * do próprio cartão, pelo nome. Não carrega o texto recebido, e a mensagem lista os nomes aceitos.
 */
public record ErroTipoDeCartaoInvalido() implements ErroValidacao {

    /** Código estável desta variante. */
    public static final String CODIGO = "CPT-VAL-CRT-003";

    @Override
    public String codigo() {
        return CODIGO;
    }

    /** Os nomes aceitos, separados por vírgula, na ordem do enum. */
    public static final String NOMES_ACEITOS = Arrays.stream(TipoCartao.values())
            .map(TipoCartao::name).collect(Collectors.joining(", "));

    @Override
    public String mensagem() {
        return "Tipo de cartão inválido: use " + NOMES_ACEITOS + ".";
    }

    @Override
    public Map<String, List<String>> camposComErro() {
        return Map.of("tipo", List.of("deve ser " + NOMES_ACEITOS));
    }
}
