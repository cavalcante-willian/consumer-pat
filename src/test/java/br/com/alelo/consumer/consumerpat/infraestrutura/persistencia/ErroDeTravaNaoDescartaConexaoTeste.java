package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia;

import com.zaxxer.hikari.SQLExceptionOverride;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class ErroDeTravaNaoDescartaConexaoTeste {

    private final ErroDeTravaNaoDescartaConexao avaliador = new ErroDeTravaNaoDescartaConexao();

    @Test
    void deve_manter_a_conexao_quando_o_erro_e_o_estouro_da_espera_por_trava() {
        // Preparar
        var erro = new SQLException("Timeout trying to lock table", "HYT00",
                ErroDeTravaNaoDescartaConexao.CODIGO_DE_TEMPO_DE_TRAVA_DO_H2);

        // Agir
        var decisao = avaliador.adjudicate(erro);

        // Verificar
        assertThat(decisao).isEqualTo(SQLExceptionOverride.Override.DO_NOT_EVICT);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 8000, 50199, 50201, 90020, 90131 })
    void deve_seguir_o_criterio_do_pool_quando_o_erro_e_outro(int codigo) {
        // Preparar
        var erro = new SQLException("outro erro", "08001", codigo);

        // Agir
        var decisao = avaliador.adjudicate(erro);

        // Verificar
        assertThat(decisao).isEqualTo(SQLExceptionOverride.Override.CONTINUE_EVICT);
    }

    @Test
    void deve_seguir_o_criterio_do_pool_quando_o_erro_nao_tem_codigo() {
        // Agir
        var decisao = avaliador.adjudicate(new SQLException("sem código"));

        // Verificar
        assertThat(decisao).isEqualTo(SQLExceptionOverride.Override.CONTINUE_EVICT);
    }
}
