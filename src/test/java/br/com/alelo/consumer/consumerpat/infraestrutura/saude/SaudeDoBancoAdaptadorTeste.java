package br.com.alelo.consumer.consumerpat.infraestrutura.saude;

import br.com.alelo.consumer.consumerpat.aplicacao.fakes.SaidaDeLogFake;
import br.com.alelo.consumer.consumerpat.dominio.log.NivelDeLog;
import br.com.alelo.consumer.consumerpat.dominio.saude.DependenciaCritica;
import br.com.alelo.consumer.consumerpat.dominio.saude.EstadoDeDependencia;
import br.com.alelo.consumer.consumerpat.dominio.saude.SituacaoDeSaude;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.sql.DataSource;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Prova o adaptador contra o H2 de verdade, e contra um pool que falha: o estado, o log da causa e
 * o que nunca chega ao estado nem ao log.
 */
@DataJpaTest
class SaudeDoBancoAdaptadorTeste {

    @Autowired
    private DataSource bancoReal;

    private final SaidaDeLogFake saida = new SaidaDeLogFake();

    @Test
    void deve_ser_saudavel_quando_o_banco_real_responde() {
        // Agir
        var estado = new SaudeDoBancoAdaptador(bancoReal, saida).consultar();

        // Verificar
        assertThat(estado.situacao()).isEqualTo(SituacaoDeSaude.SAUDAVEL);
        assertThat(estado.dependencias()).containsOnlyKeys(DependenciaCritica.BANCO)
                .containsEntry(DependenciaCritica.BANCO, EstadoDeDependencia.DISPONIVEL);
    }

    @Test
    void deve_nao_registrar_nada_quando_o_banco_esta_saudavel() {
        // Agir
        new SaudeDoBancoAdaptador(bancoReal, saida).consultar();

        // Verificar: um health a cada poucos segundos não enche o log quando está tudo bem
        assertThat(saida.eventos()).isEmpty();
    }

    @Test
    void deve_ser_degradado_e_registrar_o_critico_quando_o_banco_nao_entrega_conexao()
            throws Exception {
        // Preparar
        var quebrado = mock(DataSource.class);
        when(quebrado.getConnection()).thenThrow(new SQLException(
                "Connection refused jdbc:postgresql://10.0.0.7:5432/prod password=segredo",
                "08006"));

        // Agir
        var estado = new SaudeDoBancoAdaptador(quebrado, saida).consultar();

        // Verificar
        assertThat(estado.situacao()).isEqualTo(SituacaoDeSaude.DEGRADADO);
        assertThat(estado.dependencias()).containsEntry(DependenciaCritica.BANCO,
                EstadoDeDependencia.INDISPONIVEL);
        assertThat(saida.eventos()).singleElement().satisfies(evento -> {
            assertThat(evento.nivel()).isEqualTo(NivelDeLog.CRITICO);
            assertThat(evento.evento()).isEqualTo("banco.indisponivel");
            assertThat(evento.valorDe("estadoSql")).contains("08006");
        });
    }

    @Test
    void deve_nao_levar_host_url_nem_senha_ao_log_quando_o_driver_os_cita() throws Exception {
        // Preparar
        var quebrado = mock(DataSource.class);
        when(quebrado.getConnection()).thenThrow(new SQLException(
                "Connection refused jdbc:postgresql://10.0.0.7:5432/prod password=segredo",
                "08006"));

        // Agir
        new SaudeDoBancoAdaptador(quebrado, saida).consultar();

        // Verificar: só o tipo e o estado SQL, e nunca o que o driver escreveu
        assertThat(saida.eventos().get(0).texto()).doesNotContain("10.0.0.7").doesNotContain("jdbc")
                .doesNotContain("segredo").doesNotContain("refused");
        assertThat(saida.eventos().get(0).causa()).isEmpty();
    }

    @Test
    void deve_ser_degradado_quando_a_conexao_nao_passa_na_validacao() throws Exception {
        // Preparar
        var fonte = mock(DataSource.class);
        var conexao = mock(java.sql.Connection.class);
        when(fonte.getConnection()).thenReturn(conexao);
        when(conexao.isValid(SaudeDoBancoAdaptador.PRAZO_EM_SEGUNDOS)).thenReturn(false);

        // Agir
        var estado = new SaudeDoBancoAdaptador(fonte, saida).consultar();

        // Verificar
        assertThat(estado.situacao()).isEqualTo(SituacaoDeSaude.DEGRADADO);
        assertThat(saida.eventos().get(0).valorDe("tipoDaFalha")).contains("conexao.invalida");
    }

    @Test
    void deve_validar_com_o_prazo_curto_do_contrato() {
        // Verificar: de 1 a 2 s, para o health não ficar pendurado
        assertThat(SaudeDoBancoAdaptador.PRAZO_EM_SEGUNDOS).isBetween(1, 2);
    }
}
