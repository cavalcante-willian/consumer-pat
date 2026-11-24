package br.com.alelo.consumer.consumerpat.dominio.erros;

/**
 * Papel de um erro que sabe a que categoria de falha pertence.
 *
 * <p>
 * Quem decide o que fazer com o erro (por exemplo, que resposta HTTP devolver)
 * olha a categoria, que é dado, e nunca compara texto de mensagem.
 */
public interface ComCategoria {

    /**
     * Natureza da falha, de onde sai o status HTTP.
     *
     * @return a categoria deste erro
     */
    CategoriaErro categoria();
}
