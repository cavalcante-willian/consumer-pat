package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

import br.com.alelo.consumer.consumerpat.api.configuracao.ConfiguracaoDeAuditoria;
import br.com.alelo.consumer.consumerpat.fabricas.RelogioAjustavel;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.ConfiguracaoDeRelogioDeTeste;
import org.hibernate.StaleStateException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import javax.persistence.OptimisticLockException;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Prova o que o schema do consumidor promete, e não o que o código supõe: cada restrição é
 * conferida contra o banco real, porque uma anotação que o banco ignora em silêncio não protege
 * nada.
 */
@DataJpaTest
@Import({ ConfiguracaoDeAuditoria.class, ConfiguracaoDeRelogioDeTeste.class })
class SchemaDoConsumidorTeste {

    @Autowired
    private ConsumidorRepositorioJpa repositorio;

    @Autowired
    private TestEntityManager gerenciador;

    @Autowired
    private RelogioAjustavel relogio;

    @SuppressWarnings("unchecked")
    private List<Object[]> consultar(String sql) {
        return gerenciador.getEntityManager().createNativeQuery(sql).getResultList();
    }

    @SuppressWarnings("unchecked")
    private List<String> consultarTextos(String sql) {
        return gerenciador.getEntityManager().createNativeQuery(sql).getResultList();
    }

    @Test
    void deve_recusar_o_mesmo_documento_quando_gravar_dois_consumidores() {
        // Preparar
        repositorio.saveAndFlush(ConsumidoresDeTeste.linhaComDocumento("12345678901"));

        // Agir e Verificar
        assertThatThrownBy(() -> repositorio
                .saveAndFlush(ConsumidoresDeTeste.linhaComDocumento("12345678901")))
                        .isInstanceOf(DataIntegrityViolationException.class)
                        .hasMessageContaining("UK_CONSUMIDORES_DOCUMENTO");
    }

    @Test
    void deve_aceitar_os_telefones_nulos_quando_gravar_so_com_o_celular() {
        // Agir
        var gravado = repositorio.saveAndFlush(ConsumidoresDeTeste.linha());

        // Verificar
        assertThat(gravado.getId()).isNotNull();
        assertThat(gravado.getTelefoneResidencial()).isNull();
        assertThat(gravado.getTelefone()).isNull();
    }

    @Test
    void deve_recusar_nulo_quando_falta_o_celular() {
        // Preparar
        var linha = ConsumidoresDeTeste.linha();
        linha.setCelular(null);

        // Agir e Verificar
        assertThatThrownBy(() -> repositorio.saveAndFlush(linha))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deve_recusar_nulo_quando_falta_o_email() {
        // Preparar
        var linha = ConsumidoresDeTeste.linha();
        linha.setEmail(null);

        // Agir e Verificar
        assertThatThrownBy(() -> repositorio.saveAndFlush(linha))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deve_ter_o_tamanho_do_tipo_do_dominio_quando_consultar_as_colunas_de_texto() {
        // Agir
        var tamanhos = consultar("select column_name, character_maximum_length "
                + "from information_schema.columns where table_name = 'CONSUMIDORES' "
                + "and character_maximum_length is not null order by column_name");

        // Verificar
        assertThat(tamanhos).extracting(linha -> linha[0] + "=" + linha[1]).containsExactly(
                "CELULAR=15", "CEP=8", "CIDADE=100", "DOCUMENTO=14", "EMAIL=254", "NOME=150",
                "NUMERO=10", "PAIS=100", "RUA=100", "TELEFONE=15", "TELEFONE_RESIDENCIAL=15");
    }

    @Test
    void deve_ter_o_indice_por_nome_e_id_quando_consultar_os_indices() {
        // Agir
        var colunas = consultarTextos("select column_name from information_schema.index_columns "
                + "where table_name = 'CONSUMIDORES' and index_name = 'IDX_CONSUMIDORES_NOME_ID' "
                + "order by ordinal_position");

        // Verificar
        assertThat(colunas).containsExactly("NOME", "ID");
    }

    @Test
    void deve_comecar_na_versao_zero_e_avancar_quando_atualizar() {
        // Preparar
        var gravado = gerenciador.persistAndFlush(ConsumidoresDeTeste.linha());
        assertThat(gravado.getVersao()).isZero();

        // Agir
        gravado.setNome("Outro Nome");
        gerenciador.flush();

        // Verificar
        assertThat(gravado.getVersao()).isEqualTo(1);
    }

    @Test
    void deve_recusar_a_gravacao_quando_outra_transacao_avancou_a_versao_no_meio() {
        // Preparar: a linha é lida, e outro cliente a atualiza por fora antes da gravação
        var lida = gerenciador.persistAndFlush(ConsumidoresDeTeste.linha());
        gerenciador.getEntityManager()
                .createNativeQuery("update consumidores set versao = versao + 1").executeUpdate();

        // Agir
        lida.setNome("Nome Velho");

        // Verificar: a versão de linha pega a corrida que a conferência do adaptador não vê
        assertThatThrownBy(() -> gerenciador.flush()).isInstanceOf(OptimisticLockException.class)
                .hasCauseInstanceOf(StaleStateException.class).hasMessageContaining("versao=?");
    }

    @Test
    void deve_preencher_a_auditoria_quando_criar_e_avancar_ao_atualizar() {
        // Preparar
        var inicio = relogio.instant();
        var linha = gerenciador.persistAndFlush(ConsumidoresDeTeste.linha());
        assertThat(linha.getDataCriacao()).isEqualTo(inicio);
        assertThat(linha.getDataAtualizacao()).isEqualTo(inicio);

        // Agir
        relogio.avancar(Duration.ofMinutes(5));
        linha.setNome("Outro Nome");
        gerenciador.flush();

        // Verificar
        assertThat(linha.getDataCriacao()).isEqualTo(inicio);
        assertThat(linha.getDataAtualizacao()).isEqualTo(inicio.plus(Duration.ofMinutes(5)));
    }
}
