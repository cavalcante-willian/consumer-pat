package br.com.alelo.consumer.consumerpat.dominio.consumidor.endereco;

import br.com.alelo.consumer.consumerpat.dominio.texto.TextoLimitado;

import java.util.Objects;

/**
 * Onde o consumidor mora.
 *
 * <p>
 * O número do imóvel é texto, e não inteiro, porque existe {@code S/N} e {@code 12-B}. O limite de
 * tamanho de cada texto é o da coluna do banco, de modo que um texto maior é recusado aqui, e não
 * truncado ou estourado na gravação.
 *
 * @param rua    a rua, não vazia
 * @param numero o número do imóvel, como texto
 * @param cidade a cidade, não vazia
 * @param pais   o país, não vazio
 * @param cep    o CEP
 */
public record Endereco(String rua, String numero, String cidade, String pais, Cep cep) {

    /** Maior quantidade de caracteres de um número de imóvel. */
    public static final int MAXIMO_DO_NUMERO = 10;

    /** Maior quantidade de caracteres da rua, da cidade e do país. */
    public static final int MAXIMO_DO_TEXTO = 100;

    /**
     * Cria o endereço.
     *
     * @param rua    a rua
     * @param numero o número do imóvel
     * @param cidade a cidade
     * @param pais   o país
     * @param cep    o CEP
     * @throws IllegalArgumentException se algum texto é vazio ou passa do limite
     * @throws NullPointerException     se falta o CEP
     */
    public Endereco {
        rua = TextoLimitado.exigir(rua, "A rua", MAXIMO_DO_TEXTO);
        numero = TextoLimitado.exigir(numero, "O número", MAXIMO_DO_NUMERO);
        cidade = TextoLimitado.exigir(cidade, "A cidade", MAXIMO_DO_TEXTO);
        pais = TextoLimitado.exigir(pais, "O país", MAXIMO_DO_TEXTO);
        Objects.requireNonNull(cep, "O endereço precisa de um CEP");
    }
}
