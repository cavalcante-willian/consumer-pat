package br.com.alelo.consumer.consumerpat.api.comum.problema;

/**
 * Os problemas que nascem na borda ou no framework, e não no domínio: código, título e status
 * juntos, de modo que esquecer um deles não compila.
 *
 * <p>
 * Os problemas do domínio têm o código e o status na própria variante de erro e na categoria. Estes
 * não passam por {@code ErroDominio}: são uma entrada malformada, uma rota que não existe, um
 * conflito de concorrência ou uma falha que ninguém previu. O código segue o mesmo formato dos
 * demais, e um teste o confere.
 */
public enum ProblemaDeBorda {

    /** O corpo não passou na validação, ou não pôde ser lido. */
    ENTRADA_INVALIDA("CPT-VAL-SIS-001", "Entrada inválida", 400),

    /** O método HTTP não existe para a rota. */
    METODO_NAO_SUPORTADO("CPT-VAL-SIS-002", "Método não suportado", 405),

    /** O tipo de mídia do corpo não é o que a rota aceita. */
    MIDIA_NAO_SUPORTADA("CPT-VAL-SIS-003", "Tipo de mídia não suportado", 415),

    /** A rota não existe. */
    ROTA_NAO_ENCONTRADA("CPT-NEN-SIS-001", "Rota não encontrada", 404),

    /** Duas requisições alteraram o mesmo dado ao mesmo tempo, e a versão de linha impediu. */
    CONFLITO_DE_CONCORRENCIA("CPT-CNF-SIS-001", "Conflito de concorrência", 409),

    /** O cartão está em uso por outra requisição, e a espera pela trava estourou. */
    RECURSO_EM_USO("CPT-CNF-SIS-002", "Recurso em uso", 409),

    /** Falha que ninguém previu. O corpo nunca diz qual foi. */
    FALHA_INESPERADA("CPT-INT-SIS-001", "Falha inesperada", 500);

    private final String codigo;

    private final String titulo;

    private final int statusHttp;

    ProblemaDeBorda(String codigo, String titulo, int statusHttp) {
        this.codigo = codigo;
        this.titulo = titulo;
        this.statusHttp = statusHttp;
    }

    /**
     * O código estável do problema.
     *
     * @return o código, no formato dos erros de domínio
     */
    public String codigo() {
        return codigo;
    }

    /**
     * O título humano.
     *
     * @return o título
     */
    public String titulo() {
        return titulo;
    }

    /**
     * O status HTTP da resposta.
     *
     * @return o status
     */
    public int statusHttp() {
        return statusHttp;
    }
}
