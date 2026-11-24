package br.com.alelo.consumer.consumerpat.dominio.consumidor.contato;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContatoTeste {

    private static final Telefone CELULAR = new Telefone("11999998888");
    private static final Email EMAIL = new Email("maria@exemplo.com");

    @Test
    void deve_aceitar_os_dois_telefones_ausentes_quando_so_o_celular_e_informado() {
        // Agir
        var contato = new Contato(CELULAR, Optional.empty(), Optional.empty(), EMAIL);

        // Verificar
        assertThat(contato.telefoneResidencial()).isEmpty();
        assertThat(contato.telefone()).isEmpty();
    }

    @Test
    void deve_guardar_os_telefones_quando_eles_sao_informados() {
        // Agir
        var residencial = new Telefone("1133334444");
        var contato = new Contato(CELULAR, Optional.of(residencial), Optional.empty(), EMAIL);

        // Verificar
        assertThat(contato.telefoneResidencial()).contains(residencial);
    }

    @Test
    void deve_lancar_quando_falta_o_celular() {
        // Agir e Verificar
        assertThatThrownBy(() -> new Contato(null, Optional.empty(), Optional.empty(), EMAIL))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deve_lancar_quando_falta_o_email() {
        // Agir e Verificar
        assertThatThrownBy(() -> new Contato(CELULAR, Optional.empty(), Optional.empty(), null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deve_lancar_quando_o_opcional_e_nulo_em_vez_de_vazio() {
        // Agir e Verificar
        assertThatThrownBy(() -> new Contato(CELULAR, null, Optional.empty(), EMAIL))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Contato(CELULAR, Optional.empty(), null, EMAIL))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deve_nunca_imprimir_os_dados_inteiros_quando_o_contato_vai_para_o_log() {
        // Agir
        var texto = new Contato(CELULAR, Optional.empty(), Optional.empty(), EMAIL).toString();

        // Verificar
        assertThat(texto).doesNotContain("99999").doesNotContain("maria@");
    }
}
