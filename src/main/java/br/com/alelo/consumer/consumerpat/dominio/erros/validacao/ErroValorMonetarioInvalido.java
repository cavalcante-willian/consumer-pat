package br.com.alelo.consumer.consumerpat.dominio.erros.validacao;

/**
 * O texto recebido não é uma quantia em dinheiro válida.
 *
 * <p>
 * Não carrega o texto recebido: um valor rejeitado pode vir de qualquer campo, e devolvê-lo na
 * mensagem de erro é uma forma de vazar dado. A mensagem diz só o formato esperado, o que é
 * suficiente para quem enviou corrigir.
 */
public record ErroValorMonetarioInvalido() implements ErroValidacao {

    /** Código estável desta variante. */
    public static final String CODIGO = "CPT-VAL-DIN-001";

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String mensagem() {
        return "Valor monetário inválido: informe um número decimal com ponto, de até 13 dígitos "
                + "inteiros e 6 casas decimais.";
    }
}
