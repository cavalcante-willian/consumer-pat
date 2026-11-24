package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova que toda restrição única, toda chave estrangeira e todo índice do schema do cartão tem
 * nome, e que o nome diz o que é. Sem migração, o nome na anotação é o que torna o schema gerado
 * auditável: um nome inventado pelo Hibernate não aparece em nenhuma leitura.
 *
 * <p>
 * Ficam de fora a chave primária, que o banco nomeia, e a restrição de saldo não negativo, que o
 * Hibernate 5 não deixa nomear pela anotação.
 */
@DataJpaTest
class NomenclaturaDeSchemaTeste {

    private static final String TABELAS = "('CARTOES', 'EXTRATOS')";

    @Autowired
    private TestEntityManager gerenciador;

    @SuppressWarnings("unchecked")
    private List<String> nomesDa(String consulta) {
        return gerenciador.getEntityManager().createNativeQuery(consulta).getResultList();
    }

    private List<String> nomesDasRestricoes(String tipo) {
        return nomesDa("select constraint_name from information_schema.table_constraints "
                + "where table_name in " + TABELAS + " and constraint_type = '" + tipo + "'");
    }

    @Test
    void deve_nomear_com_prefixo_uk_quando_consultar_as_restricoes_unicas() {
        // Agir
        var nomes = nomesDasRestricoes("UNIQUE");

        // Verificar
        assertThat(nomes).isNotEmpty().allSatisfy(nome -> assertThat(nome).startsWith("UK_"));
    }

    @Test
    void deve_nomear_com_prefixo_fk_quando_consultar_as_chaves_estrangeiras() {
        // Agir
        var nomes = nomesDasRestricoes("FOREIGN KEY");

        // Verificar
        assertThat(nomes).isNotEmpty().allSatisfy(nome -> assertThat(nome).startsWith("FK_"));
    }

    @Test
    void deve_nomear_com_prefixo_conhecido_quando_consultar_os_indices() {
        // Agir
        var nomes = nomesDa("select index_name from information_schema.indexes "
                + "where table_name in " + TABELAS + " and index_type_name <> 'PRIMARY KEY'");

        // Verificar
        assertThat(nomes).isNotEmpty()
                .allSatisfy(nome -> assertThat(nome).matches("(UK|FK|IDX)_.*"));
    }
}
