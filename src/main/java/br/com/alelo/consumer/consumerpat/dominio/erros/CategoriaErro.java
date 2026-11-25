package br.com.alelo.consumer.consumerpat.dominio.erros;

/**
 * Natureza de uma falha de negócio, com o status HTTP que ela produz.
 *
 * <p>
 * O status é dado da categoria: escolhê-lo é consultar o enum, e não ramificar sobre o tipo do
 * erro. Uma categoria só existe quando há um erro concreto que a use; as demais entram com o
 * primeiro caso de uso que as produz.
 */
public enum CategoriaErro {

    /** A entrada tem forma inválida: não é um valor do tipo esperado. */
    VALIDACAO(400),

    /** A entrada é válida, mas uma regra de negócio a recusa diante do estado atual. */
    REGRA_DE_NEGOCIO(422),

    /** A entrada é válida, mas o que ela nomeia não existe. */
    NAO_ENCONTRADO(404),

    /** A entrada é válida, mas colide com o que já existe ou com uma escrita anterior. */
    CONFLITO(409),

    /** A entrada é válida, mas uma dependência crítica não responde: quem pediu nada errou. */
    INDISPONIVEL(503);

    private final int statusHttp;

    CategoriaErro(int statusHttp) {
        this.statusHttp = statusHttp;
    }

    /**
     * Status HTTP da resposta que um erro desta categoria produz.
     *
     * @return o código de status
     */
    public int statusHttp() {
        return statusHttp;
    }
}
