package br.com.alelo.consumer.consumerpat.dominio.erros;

/**
 * Papel de um erro que se explica em texto.
 */
public interface ComMensagem {

    /**
     * Texto para uma pessoa ler. Nunca contém dado sensível por inteiro: o que for identificador
     * (como número de cartão) aparece mascarado.
     *
     * @return a explicação do erro
     */
    String mensagem();
}
