package br.com.alelo.consumer.consumerpat.dominio.erros.indisponivel;

import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErroBancoIndisponivelTeste {

    private static final ErroBancoIndisponivel ERRO = new ErroBancoIndisponivel(
            "java.sql.SQLTransientConnectionException", "08001");

    @Test
    void deve_ser_da_categoria_indisponivel_com_status_503() {
        // Verificar
        assertThat(ERRO.categoria()).isEqualTo(CategoriaErro.INDISPONIVEL);
        assertThat(ERRO.categoria().statusHttp()).isEqualTo(503);
    }

    @Test
    void deve_expor_o_codigo_da_constante_quando_consultar_o_codigo() {
        // Verificar
        assertThat(ERRO.codigo()).isEqualTo(ErroBancoIndisponivel.CODIGO)
                .isEqualTo("CPT-BAN-SIS-001");
    }

    @Test
    void deve_ter_a_mesma_mensagem_qualquer_que_seja_a_causa_quando_consultar_a_mensagem() {
        // Agir
        var outra = new ErroBancoIndisponivel("conexao.invalida", "desconhecido");

        // Verificar: a mensagem é pública, então não depende do que o driver disse
        assertThat(ERRO.mensagem()).isEqualTo(outra.mensagem());
        assertThat(ERRO.mensagem()).doesNotContain("SQL").doesNotContain("08001")
                .doesNotContain("java.");
    }

    @Test
    void deve_guardar_o_tipo_e_o_estado_sql_para_o_log_quando_consultar_os_campos() {
        // Verificar
        assertThat(ERRO.tipoDaFalha()).isEqualTo("java.sql.SQLTransientConnectionException");
        assertThat(ERRO.estadoSql()).isEqualTo("08001");
    }

    @Test
    void deve_nao_apontar_campo_da_entrada_quando_a_causa_nao_e_da_entrada() {
        // Verificar
        assertThat(ERRO.camposComErro()).isEmpty();
    }

    @Test
    void deve_ter_o_estado_sql_padrao_estavel_quando_o_driver_nao_informa() {
        // Verificar
        assertThat(ErroBancoIndisponivel.SEM_ESTADO_SQL).isEqualTo("desconhecido");
    }
}
