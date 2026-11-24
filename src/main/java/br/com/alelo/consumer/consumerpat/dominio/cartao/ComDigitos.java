package br.com.alelo.consumer.consumerpat.dominio.cartao;

/**
 * Papel de um dado identificado por uma sequência de dígitos.
 *
 * <p>
 * É a operação-base de que as outras são escritas: mascarar só precisa ler os dígitos, e por isso
 * cada operação vive em seu arquivo, sem depender de como o tipo os guarda.
 */
public interface ComDigitos {

    /**
     * Os dígitos, na forma canônica: sem espaço, hífen ou qualquer outro separador.
     *
     * @return os dígitos por inteiro; por isso nunca devem ser impressos em log
     */
    String digitos();
}
