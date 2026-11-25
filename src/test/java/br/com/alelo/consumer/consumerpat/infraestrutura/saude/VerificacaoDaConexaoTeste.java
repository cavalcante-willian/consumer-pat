package br.com.alelo.consumer.consumerpat.infraestrutura.saude;

import br.com.alelo.consumer.consumerpat.dominio.erros.indisponivel.ErroBancoIndisponivel;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import br.com.alelo.consumer.consumerpat.dominio.saude.EstadoDeDependencia;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Prova a fronteira com o JDBC: a conexão é validada com o prazo pedido, devolvida ao pool em todo
 * caso, e a falha do driver vira o erro de domínio, e não uma exceção.
 */
class VerificacaoDaConexaoTeste {

    private final DataSource fonte = mock(DataSource.class);
    private final Connection conexao = mock(Connection.class);

    private static HikariDataSource poolDeUmaConexao(long esperaEmMs) {
        var configuracao = new HikariConfig();
        configuracao.setJdbcUrl("jdbc:h2:mem:saude-" + UUID.randomUUID());
        configuracao.setUsername("sa");
        configuracao.setMaximumPoolSize(1);
        configuracao.setConnectionTimeout(esperaEmMs);
        return new HikariDataSource(configuracao);
    }

    @Test
    void deve_ser_disponivel_e_validar_com_o_prazo_pedido_quando_o_banco_responde()
            throws Exception {
        // Preparar
        when(fonte.getConnection()).thenReturn(conexao);
        when(conexao.isValid(2)).thenReturn(true);

        // Agir
        var resultado = VerificacaoDaConexao.verificar(fonte, 2);

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isEqualTo(EstadoDeDependencia.DISPONIVEL);
        verify(conexao).isValid(2);
    }

    @Test
    void deve_devolver_a_conexao_ao_pool_quando_ela_e_valida() throws Exception {
        // Preparar
        when(fonte.getConnection()).thenReturn(conexao);
        when(conexao.isValid(2)).thenReturn(true);

        // Agir
        VerificacaoDaConexao.verificar(fonte, 2);

        // Verificar
        verify(conexao).close();
    }

    @Test
    void deve_devolver_a_conexao_ao_pool_quando_a_validacao_falha() throws Exception {
        // Preparar
        when(fonte.getConnection()).thenReturn(conexao);
        when(conexao.isValid(2)).thenThrow(new SQLException("estourou", "08S01"));

        // Agir
        VerificacaoDaConexao.verificar(fonte, 2);

        // Verificar: um health que vaza conexão esgota o pool que ele mede
        verify(conexao).close();
    }

    @Test
    void deve_ser_erro_quando_a_conexao_nao_passa_na_validacao() throws Exception {
        // Preparar
        when(fonte.getConnection()).thenReturn(conexao);
        when(conexao.isValid(2)).thenReturn(false);

        // Agir
        var resultado = VerificacaoDaConexao.verificar(fonte, 2);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroBancoIndisponivel.class);
    }

    @Test
    void deve_ser_erro_com_o_tipo_e_o_estado_quando_o_pool_nao_entrega_a_conexao()
            throws Exception {
        // Preparar
        when(fonte.getConnection())
                .thenThrow(new java.sql.SQLTransientConnectionException("sem conexão", "08001"));

        // Agir
        var resultado = VerificacaoDaConexao.verificar(fonte, 2);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro())
                .hasFieldOrPropertyWithValue("tipoDaFalha",
                        "java.sql.SQLTransientConnectionException")
                .hasFieldOrPropertyWithValue("estadoSql", "08001");
    }

    @Test
    void deve_ser_erro_sem_lancar_quando_a_validacao_lanca_sql_exception() throws Exception {
        // Preparar
        when(fonte.getConnection()).thenReturn(conexao);
        when(conexao.isValid(2)).thenThrow(new SQLException("Connection refused 10.0.0.7"));

        // Agir
        var resultado = VerificacaoDaConexao.verificar(fonte, 2);

        // Verificar: depois da fronteira nada lança, e a mensagem do driver não vai adiante
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro().toString()).doesNotContain("10.0.0.7");
    }

    @Test
    void deve_ser_disponivel_quando_o_pool_real_tem_conexao_livre() {
        // Preparar
        try (var pool = poolDeUmaConexao(250)) {
            // Agir
            var resultado = VerificacaoDaConexao.verificar(pool, 2);

            // Verificar
            assertInstanceOf(Sucesso.class, resultado);
        }
    }

    @Test
    void deve_ser_erro_quando_o_pool_real_esta_esgotado_e_voltar_quando_a_conexao_e_devolvida()
            throws Exception {
        // Preparar: a única conexão do pool fica em uso
        try (var pool = poolDeUmaConexao(250); var emUso = pool.getConnection()) {
            // Agir: o health pede outra, e o pool não tem
            var esgotado = VerificacaoDaConexao.verificar(pool, 2);

            // Verificar: o health falha, e esse é o sinal certo, e não um defeito dele
            var falha = assertInstanceOf(Falha.class, esgotado);
            assertThat(falha.erro()).hasFieldOrPropertyWithValue("tipoDaFalha",
                    "java.sql.SQLTransientConnectionException");
            assertThat(emUso.isClosed()).isFalse();
        }
    }

    @Test
    void deve_voltar_a_ser_disponivel_quando_o_pool_real_recupera_a_conexao() throws Exception {
        // Preparar
        try (var pool = poolDeUmaConexao(250)) {
            var emUso = pool.getConnection();
            assertInstanceOf(Falha.class, VerificacaoDaConexao.verificar(pool, 2));

            // Agir
            emUso.close();
            var resultado = VerificacaoDaConexao.verificar(pool, 2);

            // Verificar: o health não deixou a conexão presa, e o pool se recuperou
            assertInstanceOf(Sucesso.class, resultado);
        }
    }
}
