package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class MarcadoresDeVazamentoTeste {

    @Test
    void deve_dar_vazio_quando_o_corpo_so_diz_o_que_o_cliente_fez() {
        // Agir e Verificar
        assertThat(MarcadoresDeVazamento
                .achadosEm("{\"type\":\"urn:erro\",\"title\":\"Entrada inválida\",\"status\":400}"))
                        .isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = { "java.lang.NullPointerException", "org.hibernate.exception.X",
            "org.springframework.web.X", "com.fasterxml.jackson.core.X", "java.sql.SQLException",
            "JDBC connection", "Hibernate: select", "syntax error in SQL statement" })
    void deve_achar_o_marcador_quando_o_corpo_traz_nome_de_classe_ou_texto_de_banco(String corpo) {
        // Agir e Verificar
        assertThat(MarcadoresDeVazamento.achadosEm(corpo)).isNotEmpty();
    }

    @Test
    void deve_achar_a_pilha_quando_o_corpo_a_traz_com_tabulacao_de_verdade() {
        // Agir e Verificar
        assertThat(MarcadoresDeVazamento.achadosEm("erro\n\tat br.com.alelo.X.y(X.java:1)"))
                .contains("\tat ", "at br.com");
    }

    @Test
    void deve_achar_a_pilha_quando_o_json_a_traz_com_a_tabulacao_escapada() {
        // Agir e Verificar: num corpo JSON a tabulação é a barra e o t, e não o caractere
        assertThat(MarcadoresDeVazamento.achadosEm("{\"trace\":\"erro\\n\\tat br.com.alelo.X\"}"))
                .contains("\\tat ");
    }

    @Test
    void deve_dizer_cada_marcador_uma_vez_e_na_ordem_da_lista_quando_o_corpo_traz_varios() {
        // Agir
        var achados = MarcadoresDeVazamento
                .achadosEm("java.lang.Exception org.hibernate.X Hibernate Exception");

        // Verificar
        assertThat(achados).doesNotHaveDuplicates().containsSubsequence("Exception",
                "org.hibernate", "java.lang", "Hibernate");
    }
}
