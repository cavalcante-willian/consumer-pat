package br.com.alelo.consumer.consumerpat.infraestrutura.saude;

import br.com.alelo.consumer.consumerpat.aplicacao.fakes.SaidaDeLogFake;
import br.com.alelo.consumer.consumerpat.dominio.erros.indisponivel.ErroBancoIndisponivel;
import br.com.alelo.consumer.consumerpat.dominio.log.NivelDeLog;
import br.com.alelo.consumer.consumerpat.dominio.saude.EstadoDeDependencia;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegistroDaIndisponibilidadeTeste {

    private final SaidaDeLogFake saida = new SaidaDeLogFake();
    private final ErroBancoIndisponivel erro = new ErroBancoIndisponivel(
            "java.sql.SQLTransientConnectionException", "08001");

    @Test
    void deve_registrar_banco_indisponivel_no_nivel_critico_quando_o_banco_nao_responde() {
        // Agir
        RegistroDaIndisponibilidade.registrar(saida, erro);

        // Verificar
        assertThat(saida.eventos()).singleElement().satisfies(evento -> {
            assertThat(evento.nivel()).isEqualTo(NivelDeLog.CRITICO);
            assertThat(evento.evento()).isEqualTo("banco.indisponivel");
            assertThat(evento.valorDe("operacao")).contains("saude");
            assertThat(evento.valorDe("tipoDaFalha"))
                    .contains("java.sql.SQLTransientConnectionException");
            assertThat(evento.valorDe("estadoSql")).contains("08001");
        });
    }

    @Test
    void deve_devolver_indisponivel_quando_registrar() {
        // Agir
        var estado = RegistroDaIndisponibilidade.registrar(saida, erro);

        // Verificar
        assertThat(estado).isEqualTo(EstadoDeDependencia.INDISPONIVEL);
    }
}
