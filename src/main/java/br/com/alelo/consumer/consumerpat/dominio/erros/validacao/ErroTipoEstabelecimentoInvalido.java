package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import java.util.List;
import java.util.Map;

/**
 * O código de tipo de estabelecimento recebido não corresponde a nenhum tipo conhecido.
 *
 * <p>
 * É erro de validação, e não de estado: o código 99 não é um tipo que "não foi achado", é um valor
 * que não existe, e por isso a resposta é a mesma de um campo malformado. Aponta o campo
 * {@code tipoEstabelecimento}, para que a resposta o liste como qualquer outro erro de entrada.
 *
 * @param codigoRecebido o código que veio na requisição; é um número de tipo, e não um dado
 *                       sensível
 */
public record ErroTipoEstabelecimentoInvalido(int codigoRecebido) implements ErroValidacao {

    /** Código estável desta variante. */
    public static final String CODIGO = "CPT-VAL-CRT-001";

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String mensagem() {
        return "O tipo de estabelecimento informado não existe.";
    }

    @Override
    public Map<String, List<String>> camposComErro() {
        return Map.of("tipoEstabelecimento",
                List.of("não corresponde a um tipo de estabelecimento conhecido"));
    }
}
