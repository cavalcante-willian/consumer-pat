package br.com.alelo.consumer.consumerpat.infraestrutura.saude;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;

import static org.assertj.core.api.Assertions.assertThat;

class ErroBancoIndisponivelDeSqlTeste {

    @Test
    void deve_levar_o_tipo_e_o_estado_sql_quando_o_driver_informa() {
        // Agir
        var erro = ErroBancoIndisponivelDeSql
                .converter(new SQLTransientConnectionException("falhou", "08001"));

        // Verificar
        assertThat(erro.tipoDaFalha()).isEqualTo("java.sql.SQLTransientConnectionException");
        assertThat(erro.estadoSql()).isEqualTo("08001");
    }

    @Test
    void deve_usar_o_estado_padrao_quando_o_driver_nao_informa_nenhum() {
        // Agir
        var erro = ErroBancoIndisponivelDeSql.converter(new SQLException("sem estado"));

        // Verificar
        assertThat(erro.estadoSql()).isEqualTo("desconhecido");
    }

    @Test
    void deve_deixar_a_mensagem_da_excecao_de_fora_quando_ela_cita_host_e_url() {
        // Preparar
        var falha = new SQLException("Connection refused jdbc:postgresql://10.0.0.7:5432/prod "
                + "user=admin password=segredo", "08006");

        // Agir
        var erro = ErroBancoIndisponivelDeSql.converter(falha);

        // Verificar: nada do que o driver escreveu sobrevive, nem no toString do erro
        assertThat(erro.toString()).doesNotContain("10.0.0.7").doesNotContain("jdbc")
                .doesNotContain("segredo").doesNotContain("admin").doesNotContain("refused");
        assertThat(erro.mensagem()).doesNotContain("10.0.0.7");
    }
}
