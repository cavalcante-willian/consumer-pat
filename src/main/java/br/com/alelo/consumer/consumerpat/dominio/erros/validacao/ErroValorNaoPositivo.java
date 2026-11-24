package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import java.util.List;
import java.util.Map;

/**
 * O valor informado é zero ou negativo, e a operação só aceita uma quantia maior que zero.
 *
 * <p>
 * É erro de validação, e não de regra de negócio: um débito de valor negativo não é uma operação
 * que o estado recusa, é um valor que não faz sentido para ela. Aponta o campo {@code valor}. Não
 * carrega o valor recebido, que é só um número que o cliente acabou de enviar.
 */
public record ErroValorNaoPositivo() implements ErroValidacao {

    /** Código estável desta variante. */
    public static final String CODIGO = "CPT-VAL-DIN-002";

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String mensagem() {
        return "Valor inválido: informe uma quantia maior que zero.";
    }

    @Override
    public Map<String, List<String>> camposComErro() {
        return Map.of("valor", List.of("deve ser maior que zero"));
    }
}
