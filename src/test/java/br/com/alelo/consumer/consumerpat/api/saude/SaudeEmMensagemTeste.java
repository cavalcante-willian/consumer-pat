package br.com.alelo.consumer.consumerpat.api.saude;

import br.com.alelo.consumer.consumerpat.dominio.saude.DependenciaCritica;
import br.com.alelo.consumer.consumerpat.dominio.saude.EstadoDeDependencia;
import br.com.alelo.consumer.consumerpat.dominio.saude.EstadoDeSaude;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SaudeEmMensagemTeste {

    @Test
    void deve_nomear_a_situacao_e_cada_dependencia_quando_o_banco_esta_disponivel() {
        // Agir
        var mensagem = SaudeEmMensagem.montar(new EstadoDeSaude(
                Map.of(DependenciaCritica.BANCO, EstadoDeDependencia.DISPONIVEL)));

        // Verificar
        assertThat(mensagem.estado()).isEqualTo("SAUDAVEL");
        assertThat(mensagem.dependencias()).containsExactly(Map.entry("banco", "DISPONIVEL"));
    }

    @Test
    void deve_nomear_a_situacao_e_cada_dependencia_quando_o_banco_esta_indisponivel() {
        // Agir
        var mensagem = SaudeEmMensagem.montar(new EstadoDeSaude(
                Map.of(DependenciaCritica.BANCO, EstadoDeDependencia.INDISPONIVEL)));

        // Verificar
        assertThat(mensagem.estado()).isEqualTo("DEGRADADO");
        assertThat(mensagem.dependencias()).containsExactly(Map.entry("banco", "INDISPONIVEL"));
    }
}
