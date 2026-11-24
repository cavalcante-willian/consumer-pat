package br.com.alelo.consumer.consumerpat.dominio.erros;

/**
 * Papel de um erro que tem um código estável.
 */
public interface ComCodigo {

    /**
     * Código estável da variante, no formato {@code CPT-<CATEGORIA>-<ÁREA>-<SEQUENCIAL>}.
     *
     * <p>
     * Aparece em log, em resposta e em chamado de suporte, então nunca muda e nunca é reutilizado
     * por outra variante.
     *
     * @return o código desta variante
     */
    String codigo();
}
