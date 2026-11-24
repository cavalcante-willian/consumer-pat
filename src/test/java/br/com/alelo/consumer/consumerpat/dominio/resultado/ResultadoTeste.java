package br.com.alelo.consumer.consumerpat.dominio.resultado;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultadoTeste {

    @Test
    void deve_ter_so_sucesso_e_falha_como_variantes_quando_listar_as_permitidas() {
        // Agir
        var variantes = Resultado.class.getPermittedSubclasses();

        // Verificar
        assertThat(variantes).containsExactlyInAnyOrder(Sucesso.class, Falha.class);
    }

    @Test
    void deve_compor_os_tres_papeis_quando_listar_as_interfaces() {
        // Agir
        var papeis = Resultado.class.getInterfaces();

        // Verificar
        assertThat(papeis).containsExactlyInAnyOrder(Resolvivel.class, Transformavel.class, Encadeavel.class);
    }
}
