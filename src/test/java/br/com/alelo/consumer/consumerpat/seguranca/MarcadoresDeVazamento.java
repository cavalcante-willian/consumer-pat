package br.com.alelo.consumer.consumerpat.seguranca;

import java.util.List;

/**
 * Procura, no corpo de uma resposta, o que não devia estar lá: nome de classe, pilha de chamadas e
 * texto de erro de banco.
 *
 * <p>
 * Uma resposta de erro diz o que o cliente fez de errado, e não como o servidor é por dentro. Quem
 * lê uma pilha com {@code org.hibernate} ou um erro de {@code SQL} ganha um mapa do que atacar.
 */
public final class MarcadoresDeVazamento {

    private static final List<String> MARCADORES = List.of("Exception", "\tat ", "\\tat ",
            "at br.com", "org.springframework", "org.hibernate", "com.fasterxml", "java.lang",
            "java.sql", "SQLSyntax", "JDBC", "Hibernate", "H2 ", "syntax error");

    private MarcadoresDeVazamento() {
    }

    /**
     * Os marcadores que o texto contém.
     *
     * @param texto o corpo de uma resposta
     * @return os marcadores encontrados, na ordem da lista, ou vazio se o corpo está limpo
     */
    public static List<String> achadosEm(String texto) {
        return MARCADORES.stream().filter(texto::contains).toList();
    }
}
