package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.saude;

import br.com.alelo.consumer.consumerpat.aplicacao.fakes.SaudePortaFake;
import br.com.alelo.consumer.consumerpat.dominio.saude.EstadoDeDependencia;
import br.com.alelo.consumer.consumerpat.dominio.saude.SituacaoDeSaude;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

class ConsultarSaudeCasoTeste {

    private final SaudePortaFake porta = new SaudePortaFake();
    private final ConsultarSaudeCaso caso = new ConsultarSaudeCaso(porta);

    @Test
    void deve_devolver_saudavel_quando_o_banco_esta_disponivel() {
        // Agir
        var estado = caso.consultar();

        // Verificar
        assertThat(estado.situacao()).isEqualTo(SituacaoDeSaude.SAUDAVEL);
    }

    @Test
    void deve_devolver_degradado_quando_o_banco_esta_indisponivel() {
        // Preparar
        porta.banco(EstadoDeDependencia.INDISPONIVEL);

        // Agir
        var estado = caso.consultar();

        // Verificar: a dependência fora do ar é o dado devolvido, e não uma falha da consulta
        assertThat(estado.situacao()).isEqualTo(SituacaoDeSaude.DEGRADADO);
    }

    @Test
    void deve_refletir_o_estado_atual_quando_o_banco_volta() {
        // Preparar
        porta.banco(EstadoDeDependencia.INDISPONIVEL);
        caso.consultar();
        porta.banco(EstadoDeDependencia.DISPONIVEL);

        // Agir
        var estado = caso.consultar();

        // Verificar: nada é guardado entre uma consulta e a outra
        assertThat(estado.situacao()).isEqualTo(SituacaoDeSaude.SAUDAVEL);
    }

    @Test
    void deve_nao_abrir_transacao_quando_a_verificacao_nao_toca_nenhuma_tabela() throws Exception {
        // Agir
        var metodo = ConsultarSaudeCaso.class.getMethod("consultar");

        // Verificar
        assertThat(metodo.getAnnotation(Transactional.class)).isNull();
    }
}
