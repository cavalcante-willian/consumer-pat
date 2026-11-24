package br.com.alelo.consumer.consumerpat.dominio.consumidor;

import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeConsumidores;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DadosDoConsumidorTeste {

    private static final DadosDoConsumidor BASE = FabricaDeConsumidores.dados();

    private static DadosDoConsumidor comNome(String nome) {
        return new DadosDoConsumidor(nome, BASE.documento(), BASE.dataNascimento(), BASE.contato(),
                BASE.endereco());
    }

    @Test
    void deve_tirar_o_espaco_das_pontas_do_nome_quando_criar() {
        // Agir
        var dados = comNome("  Maria Souza  ");

        // Verificar
        assertThat(dados.nome()).isEqualTo("Maria Souza");
    }

    @Test
    void deve_aceitar_o_nome_do_tamanho_do_limite() {
        // Agir
        var dados = comNome("a".repeat(DadosDoConsumidor.MAXIMO_DO_NOME));

        // Verificar
        assertThat(dados.nome()).hasSize(150);
    }

    @Test
    void deve_lancar_quando_o_nome_passa_do_limite_da_coluna() {
        // Agir e Verificar
        assertThatThrownBy(() -> comNome("a".repeat(DadosDoConsumidor.MAXIMO_DO_NOME + 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_lancar_quando_o_nome_e_vazio_ou_nulo() {
        // Agir e Verificar
        assertThatThrownBy(() -> comNome("  ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> comNome(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_lancar_quando_falta_qualquer_um_dos_outros_dados() {
        // Agir e Verificar
        assertThatThrownBy(() -> new DadosDoConsumidor("Maria", null, BASE.dataNascimento(),
                BASE.contato(), BASE.endereco())).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new DadosDoConsumidor("Maria", BASE.documento(), null,
                BASE.contato(), BASE.endereco())).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new DadosDoConsumidor("Maria", BASE.documento(),
                LocalDate.of(1990, 1, 1), null, BASE.endereco()))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new DadosDoConsumidor("Maria", BASE.documento(),
                LocalDate.of(1990, 1, 1), BASE.contato(), null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deve_nao_ter_por_onde_carregar_saldo_quando_consultar_os_componentes() {
        // Verificar: a atualização do cadastro não pode mexer em dinheiro
        assertThat(DadosDoConsumidor.class.getRecordComponents())
                .extracting(componente -> componente.getName())
                .containsExactly("nome", "documento", "dataNascimento", "contato", "endereco");
    }
}
