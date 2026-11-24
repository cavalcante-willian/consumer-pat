package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao;

import br.com.alelo.consumer.consumerpat.api.configuracao.ConfiguracaoDeAuditoria;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeCartoes;
import br.com.alelo.consumer.consumerpat.fabricas.RelogioAjustavel;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.ConfiguracaoDeRelogioDeTeste;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import javax.persistence.PersistenceException;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Prova o que o schema do cartão promete, e não o que o código supõe: cada restrição é conferida
 * contra o banco real, porque uma anotação que o banco ignora em silêncio não protege nada.
 */
@DataJpaTest
@Import({ ConfiguracaoDeAuditoria.class, ConfiguracaoDeRelogioDeTeste.class })
class SchemaDoCartaoTeste {

    @Autowired
    private CartaoRepositorioJpa repositorio;

    @Autowired
    private TestEntityManager gerenciador;

    @Autowired
    private RelogioAjustavel relogio;

    @Test
    void deve_recusar_saldo_negativo_quando_gravar_direto_no_banco() {
        // Preparar
        gerenciador.persistAndFlush(
                CartoesDeTeste.linha(gerenciador, TipoCartao.ALIMENTACAO, "10.00"));

        // Agir e Verificar
        assertThatThrownBy(() -> gerenciador.getEntityManager()
                .createNativeQuery("update cartoes set saldo = -0.000001").executeUpdate())
                .isInstanceOf(PersistenceException.class);
    }

    @Test
    void deve_recusar_saldo_negativo_quando_gravar_pela_entidade() {
        // Agir e Verificar
        assertThatThrownBy(() -> repositorio
                .saveAndFlush(CartoesDeTeste.linha(gerenciador, TipoCartao.ALIMENTACAO, "-0.01")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deve_aceitar_saldo_zero_quando_gravar() {
        // Agir
        var gravado = repositorio
                .saveAndFlush(CartoesDeTeste.linha(gerenciador, TipoCartao.ALIMENTACAO, "0.00"));

        // Verificar
        assertThat(gravado.getId()).isNotNull();
    }

    @Test
    void deve_recusar_o_mesmo_numero_quando_os_tipos_sao_diferentes() {
        // Preparar
        repositorio
                .saveAndFlush(CartoesDeTeste.linha(gerenciador, TipoCartao.ALIMENTACAO, "10.00"));

        // Agir e Verificar
        assertThatThrownBy(() -> repositorio
                .saveAndFlush(CartoesDeTeste.linha(gerenciador, TipoCartao.FARMACIA, "10.00")))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("UK_CARTOES_NUMERO");
    }

    @Test
    void deve_gravar_o_tipo_como_texto_quando_persistir_o_tipo_do_cartao() {
        // Preparar
        gerenciador.persistAndFlush(
                CartoesDeTeste.linha(gerenciador, TipoCartao.COMBUSTIVEL, "10.00"));

        // Agir
        var gravado = gerenciador.getEntityManager().createNativeQuery("select tipo from cartoes")
                .getSingleResult();

        // Verificar
        assertThat(gravado).isEqualTo("COMBUSTIVEL");
    }

    @Test
    void deve_ter_a_escala_do_dinheiro_quando_consultar_as_colunas_monetarias() {
        // Agir
        @SuppressWarnings("unchecked")
        List<Object[]> escalas = gerenciador.getEntityManager()
                .createNativeQuery("select table_name || '.' || column_name, numeric_scale "
                        + "from information_schema.columns "
                        + "where (table_name = 'CARTOES' and column_name = 'SALDO') "
                        + "or (table_name = 'EXTRATOS' "
                        + "and column_name in ('VALOR_INFORMADO', 'VALOR_COBRADO'))")
                .getResultList();

        // Verificar
        assertThat(escalas).hasSize(3);
        assertThat(escalas).allSatisfy(linha -> assertThat(linha[1]).isEqualTo(Dinheiro.ESCALA));
    }

    @Test
    void deve_preencher_a_auditoria_quando_criar_e_avancar_ao_atualizar() {
        // Preparar
        var inicio = relogio.instant();
        var linha = gerenciador.persistAndFlush(
                CartoesDeTeste.linha(gerenciador, TipoCartao.ALIMENTACAO, "10.00"));
        var criacao = linha.getDataCriacao();
        assertThat(criacao).isEqualTo(inicio);
        assertThat(linha.getDataAtualizacao()).isEqualTo(inicio);

        // Agir
        relogio.avancar(Duration.ofMinutes(5));
        linha.setSaldo(FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).comSaldo("9.00").construir()
                .saldo().valor());
        gerenciador.flush();
        gerenciador.clear();

        // Verificar
        var relido = gerenciador.find(CartaoEntidade.class, linha.getId());
        assertThat(relido.getDataCriacao()).isEqualTo(criacao);
        assertThat(relido.getDataAtualizacao()).isEqualTo(inicio.plus(Duration.ofMinutes(5)));
    }
}
