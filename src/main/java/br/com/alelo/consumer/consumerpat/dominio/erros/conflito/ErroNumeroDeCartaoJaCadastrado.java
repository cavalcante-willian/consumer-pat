package br.com.alelo.consumer.consumerpat.dominio.erros.conflito;

import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;

import java.util.List;
import java.util.Map;

/**
 * Já existe um cartão com o número informado.
 *
 * <p>
 * O número é único em todo o sistema, e não por consumidor: o cartão que o consumidor traz é um
 * cartão físico, e o mesmo número em dois consumidores seria o mesmo cartão em duas mãos. O número
 * vem como {@link NumeroDeCartao}, cujo texto já é mascarado.
 *
 * @param numero o número que colidiu
 */
public record ErroNumeroDeCartaoJaCadastrado(NumeroDeCartao numero) implements ErroConflito {

    /** Código estável desta variante. */
    public static final String CODIGO = "CPT-CNF-CRT-001";

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String mensagem() {
        return "Já existe um cartão cadastrado com o número " + numero + ".";
    }

    @Override
    public Map<String, List<String>> camposComErro() {
        return Map.of("numerosCartoes", List.of("contém um número de cartão já cadastrado"));
    }
}
