package br.com.alelo.consumer.consumerpat.dominio.saude;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class DependenciaCriticaTeste {

    @Test
    void deve_chamar_o_banco_de_banco_na_resposta_do_health() {
        // Verificar
        assertThat(DependenciaCritica.BANCO.nome()).isEqualTo("banco");
    }

    @ParameterizedTest
    @EnumSource(DependenciaCritica.class)
    void deve_ter_nome_estavel_em_minusculas_e_unico_quando_consultar_qualquer_dependencia(
            DependenciaCritica dependencia) {
        // Verificar: é por ele que o balanceador lê qual dependência falhou
        assertThat(dependencia.nome()).isNotBlank().isEqualTo(dependencia.nome().toLowerCase());
        assertThat(Arrays.stream(DependenciaCritica.values()).map(DependenciaCritica::nome))
                .doesNotHaveDuplicates();
    }
}
