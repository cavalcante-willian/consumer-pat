package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import java.util.List;
import java.util.Map;

/**
 * A entrada tem mais de um campo inválido, e este erro os reúne.
 *
 * <p>
 * Um cadastro tem uma dúzia de campos, e devolver só o primeiro erro de conversão obrigaria o
 * cliente a corrigir e reenviar uma vez por campo. Este erro carrega todos de uma vez, campo a
 * campo, no mesmo formato dos erros que o Bean Validation já reúne. Não repete nenhum valor
 * recebido: cada mensagem vem de um erro de validação, que nunca o traz.
 *
 * @param campos as mensagens de cada campo inválido, por nome do campo
 */
public record ErroEntradaInvalida(Map<String, List<String>> campos) implements ErroValidacao {

    /** Código estável desta variante. */
    public static final String CODIGO = "CPT-VAL-GER-001";

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String mensagem() {
        return "Um ou mais campos precisam de correção.";
    }

    @Override
    public Map<String, List<String>> camposComErro() {
        return campos;
    }
}
