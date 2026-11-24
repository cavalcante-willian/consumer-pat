package br.com.alelo.consumer.consumerpat.dominio.consumidor.endereco;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnderecoTeste {

    private static final Cep CEP = new Cep("01001000");

    @Test
    void deve_guardar_o_numero_como_texto_quando_ele_nao_e_so_digitos() {
        // Agir
        var endereco = new Endereco("Rua das Flores", "12-B", "São Paulo", "Brasil", CEP);

        // Verificar
        assertThat(endereco.numero()).isEqualTo("12-B");
    }

    @Test
    void deve_tirar_o_espaco_das_pontas_quando_criar() {
        // Agir
        var endereco = new Endereco("  Rua A ", " S/N ", " Recife ", " Brasil ", CEP);

        // Verificar
        assertThat(endereco.rua()).isEqualTo("Rua A");
        assertThat(endereco.numero()).isEqualTo("S/N");
        assertThat(endereco.cidade()).isEqualTo("Recife");
        assertThat(endereco.pais()).isEqualTo("Brasil");
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "  " })
    @NullSource
    void deve_lancar_quando_a_rua_e_vazia(String rua) {
        // Agir e Verificar
        assertThatThrownBy(() -> new Endereco(rua, "1", "Recife", "Brasil", CEP))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "  " })
    @NullSource
    void deve_lancar_quando_a_cidade_ou_o_pais_e_vazio(String vazio) {
        // Agir e Verificar
        assertThatThrownBy(() -> new Endereco("Rua A", "1", vazio, "Brasil", CEP))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Endereco("Rua A", "1", "Recife", vazio, CEP))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_lancar_quando_o_numero_passa_do_limite_da_coluna() {
        // Agir e Verificar
        assertThatThrownBy(() -> new Endereco("Rua A", "1".repeat(Endereco.MAXIMO_DO_NUMERO + 1),
                "Recife", "Brasil", CEP)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_aceitar_o_texto_do_tamanho_do_limite_quando_a_rua_chega_no_maximo() {
        // Agir
        var endereco = new Endereco("a".repeat(Endereco.MAXIMO_DO_TEXTO), "1", "Recife", "Brasil",
                CEP);

        // Verificar
        assertThat(endereco.rua()).hasSize(Endereco.MAXIMO_DO_TEXTO);
    }

    @Test
    void deve_lancar_quando_falta_o_cep() {
        // Agir e Verificar
        assertThatThrownBy(() -> new Endereco("Rua A", "1", "Recife", "Brasil", null))
                .isInstanceOf(NullPointerException.class);
    }
}
