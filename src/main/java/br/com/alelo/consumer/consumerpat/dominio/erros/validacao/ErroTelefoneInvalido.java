package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

import java.util.List;
import java.util.Map;

/**
 * Um telefone informado não tem de 10 a 15 dígitos.
 *
 * <p>
 * Há três campos de telefone, e o erro diz de qual deles se trata. Não carrega o texto recebido.
 *
 * @param campo o nome do campo da entrada em que o telefone veio
 */
public record ErroTelefoneInvalido(String campo) implements ErroValidacao {

    /** Código estável desta variante. */
    public static final String CODIGO = "CPT-VAL-CON-002";

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String mensagem() {
        return "Telefone inválido: informe de 10 a 15 dígitos, com o DDD.";
    }

    @Override
    public Map<String, List<String>> camposComErro() {
        return Map.of(campo, List.of("deve ter de 10 a 15 dígitos, com o DDD"));
    }
}
