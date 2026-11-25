package br.com.alelo.consumer.consumerpat.infraestrutura.saude;

import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import br.com.alelo.consumer.consumerpat.dominio.saude.EstadoDeDependencia;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ValidacaoDaConexaoTeste {

    @Test
    void deve_ser_disponivel_quando_a_conexao_passa_na_validacao() {
        // Agir
        var resultado = ValidacaoDaConexao.de(true);

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isEqualTo(EstadoDeDependencia.DISPONIVEL);
    }

    @Test
    void deve_ser_o_erro_de_banco_indisponivel_quando_a_conexao_nao_passa_na_validacao() {
        // Agir
        var resultado = ValidacaoDaConexao.de(false);

        // Verificar: o driver só disse que não, sem exceção, e é tão indisponível quanto o resto
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).hasFieldOrPropertyWithValue("tipoDaFalha", "conexao.invalida")
                .hasFieldOrPropertyWithValue("estadoSql", "desconhecido");
    }
}
