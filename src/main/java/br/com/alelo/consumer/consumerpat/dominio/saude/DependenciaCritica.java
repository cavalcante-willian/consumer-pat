package br.com.alelo.consumer.consumerpat.dominio.saude;

/**
 * As dependências sem as quais o sistema não cumpre a função dele.
 *
 * <p>
 * Hoje só há uma, o banco. O nome é o que a resposta do health mostra, e por isso é estável e em
 * minúsculas: é dele que o balanceador ou o operador lê qual dependência falhou.
 */
public enum DependenciaCritica {

    /** O banco de dados, onde moram os consumidores, os cartões e o extrato. */
    BANCO("banco");

    private final String nome;

    DependenciaCritica(String nome) {
        this.nome = nome;
    }

    /**
     * O nome da dependência na resposta do health.
     *
     * @return o nome, em minúsculas
     */
    public String nome() {
        return nome;
    }
}
