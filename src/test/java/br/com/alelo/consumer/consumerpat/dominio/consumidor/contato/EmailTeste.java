package br.com.alelo.consumer.consumerpat.dominio.consumidor.contato;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTeste {

    @ParameterizedTest
    @ValueSource(
            strings = { "maria@exemplo.com", "maria.souza+beneficio@exemplo.com.br", "a@b.co" })
    void deve_aceitar_o_formato_nome_arroba_dominio_sufixo(String endereco) {
        // Agir
        var email = new Email(endereco);

        // Verificar
        assertThat(email.endereco()).isEqualTo(endereco);
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "maria", "maria@", "@exemplo.com", "maria@exemplo",
            "maria@@exemplo.com", "maria souza@exemplo.com", "maria@exemplo..com", "maria@.com" })
    @NullSource
    void deve_lancar_quando_nao_tem_o_formato(String endereco) {
        // Agir e Verificar
        assertThatThrownBy(() -> new Email(endereco)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_tirar_o_espaco_das_pontas_quando_criar() {
        // Agir
        var email = new Email("  maria@exemplo.com  ");

        // Verificar
        assertThat(email.endereco()).isEqualTo("maria@exemplo.com");
    }

    @Test
    void deve_aceitar_o_maior_tamanho_e_recusar_um_a_mais() {
        // Preparar
        var sufixo = "@exemplo.com";
        var nomeNoLimite = "a".repeat(Email.MAXIMO_DE_CARACTERES - sufixo.length());

        // Verificar
        assertThat(new Email(nomeNoLimite + sufixo).endereco()).hasSize(254);
        assertThatThrownBy(() -> new Email(nomeNoLimite + "a" + sufixo))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_esconder_o_nome_e_mostrar_so_o_dominio_quando_imprimir() {
        // Agir
        var texto = new Email("maria@exemplo.com.br").toString();

        // Verificar
        assertThat(texto).isEqualTo("***@exemplo.com.br").doesNotContain("maria");
    }

    @Test
    void deve_nunca_levar_o_valor_na_mensagem_quando_lancar() {
        // Agir e Verificar
        assertThatThrownBy(() -> new Email("maria-sem-arroba"))
                .hasMessageNotContaining("maria-sem-arroba");
    }
}
