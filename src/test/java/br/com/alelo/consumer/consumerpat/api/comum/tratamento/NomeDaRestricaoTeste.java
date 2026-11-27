package br.com.alelo.consumer.consumerpat.api.comum.tratamento;

import br.com.alelo.consumer.consumerpat.api.configuracao.ConfiguracaoDeAuditoria;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.ConfiguracaoDeRelogioDeTeste;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ConsumidoresDeTeste;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Prova contra o H2 de verdade que o nome da restrição sai sem o valor que colidiu.
 */
@DataJpaTest
@Import({ ConfiguracaoDeAuditoria.class, ConfiguracaoDeRelogioDeTeste.class })
class NomeDaRestricaoTeste {

    @Autowired
    private TestEntityManager gerenciador;

    @Test
    void deve_dar_so_o_nome_da_restricao_quando_o_h2_recusa_o_documento_repetido() {
        // Preparar
        var primeira = ConsumidoresDeTeste.linha();
        gerenciador.persistAndFlush(primeira);
        var repetida = ConsumidoresDeTeste
                .linhaComDocumento(ConsumidoresDeTeste.documentoDe(primeira));

        // Agir
        var excecao = catchThrowable(() -> gerenciador.persistAndFlush(repetida));

        // Verificar: o driver devolve a restrição com o valor colado, e o nome tem só o 1º termo
        var nome = NomeDaRestricao.de(excecao);
        assertThat(excecao).isInstanceOf(javax.persistence.PersistenceException.class);
        assertThat(nome).containsIgnoringCase("UK_CONSUMIDORES_DOCUMENTO");
        assertThat(nome).doesNotContain(ConsumidoresDeTeste.documentoDe(primeira))
                .doesNotContain("VALUES").doesNotContainAnyWhitespaces().doesNotContain("'");
    }

    @Test
    void deve_cortar_no_primeiro_termo_quando_o_nome_vem_com_a_descricao_e_o_valor() {
        // Preparar
        var causa = new ConstraintViolationException("x", new SQLException("y"),
                "PUBLIC.UK_CARTOES_NUMERO_INDEX_5 ON PUBLIC.CARTOES(NUMERO) "
                        + "VALUES ('5555000011112222')");

        // Agir
        var nome = NomeDaRestricao.de(new DataIntegrityViolationException("z", causa));

        // Verificar
        assertThat(nome).isEqualTo("PUBLIC.UK_CARTOES_NUMERO_INDEX_5");
    }

    @Test
    void deve_achar_a_restricao_quando_ela_esta_fundo_na_cadeia_de_causas() {
        // Preparar
        var causa = new ConstraintViolationException("x", new SQLException("y"), "UK_FUNDO");
        var meio = new RuntimeException("meio", causa);

        // Agir
        var nome = NomeDaRestricao.de(new DataIntegrityViolationException("topo", meio));

        // Verificar
        assertThat(nome).isEqualTo("UK_FUNDO");
    }

    @Test
    void deve_dizer_desconhecida_quando_nenhuma_causa_traz_restricao() {
        // Agir e Verificar
        assertThat(NomeDaRestricao.de(new DataIntegrityViolationException("sem causa")))
                .isEqualTo(NomeDaRestricao.DESCONHECIDA);
    }

    @Test
    void deve_dizer_desconhecida_quando_o_nome_da_restricao_e_nulo() {
        // Preparar
        var causa = new ConstraintViolationException("x", new SQLException("y"), null);

        // Agir e Verificar
        assertThat(NomeDaRestricao.de(causa)).isEqualTo(NomeDaRestricao.DESCONHECIDA);
    }

    @Test
    void deve_dizer_desconhecida_quando_o_nome_nao_tem_nenhum_caractere_aceito() {
        // Preparar
        var causa = new ConstraintViolationException("x", new SQLException("y"), "'\" ;--");

        // Agir e Verificar
        assertThat(NomeDaRestricao.de(causa)).isEqualTo(NomeDaRestricao.DESCONHECIDA);
    }
}
