package br.com.alelo.consumer.consumerpat.dominio.cartao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NumeroDeCartaoTeste {

    private static final String NUMERO = "5555000011112222";

    @ParameterizedTest
    @ValueSource(strings = { "1234567890123", "5555000011112222", "1234567890123456789" })
    void deve_aceitar_os_digitos_quando_sao_de_treze_a_dezenove(String digitos) {
        // Agir
        var numero = new NumeroDeCartao(digitos);

        // Verificar
        assertThat(numero.digitos()).isEqualTo(digitos);
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "123456789012", "12345678901234567890", "5555 0000 1111 2222",
            "5555-0000-1111-2222", "555500001111222a", "5555000011112222 " })
    @NullSource
    void deve_lancar_quando_os_digitos_nao_sao_treze_a_dezenove_digitos_sem_separador(
            String digitos) {
        // Agir e Verificar
        assertThatThrownBy(() -> new NumeroDeCartao(digitos))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_nao_incluir_o_numero_na_mensagem_quando_lancar() {
        // Preparar
        var invalido = "5555000011112222x";

        // Agir e Verificar
        assertThatThrownBy(() -> new NumeroDeCartao(invalido))
                .hasMessageNotContaining("5555000011112222");
    }

    @Test
    void deve_mostrar_so_os_quatro_ultimos_digitos_quando_mascarar() {
        // Preparar
        var numero = new NumeroDeCartao(NUMERO);

        // Agir
        var mascarado = numero.mascarado();

        // Verificar
        assertThat(mascarado).isEqualTo("****2222");
    }

    @Test
    void deve_mascarar_igual_quando_o_numero_tem_treze_ou_dezenove_digitos() {
        // Verificar
        assertThat(new NumeroDeCartao("1234567890123").mascarado()).isEqualTo("****0123");
        assertThat(new NumeroDeCartao("1234567890123456789").mascarado()).isEqualTo("****6789");
    }

    @Test
    void deve_imprimir_so_a_forma_mascarada_quando_converter_em_texto() {
        // Preparar
        var numero = new NumeroDeCartao(NUMERO);

        // Agir
        var texto = numero.toString();

        // Verificar
        assertThat(texto).isEqualTo("****2222").doesNotContain("5555000011112222");
    }

    @Test
    void deve_nao_vazar_o_numero_quando_um_objeto_que_o_contem_e_impresso() {
        // Preparar
        var compra = List.of(new NumeroDeCartao(NUMERO), "Arroz");

        // Agir
        var texto = compra.toString();

        // Verificar
        assertThat(texto).contains("****2222").doesNotContain("5555000011112222");
    }

    @Test
    void deve_ser_igual_quando_os_digitos_sao_os_mesmos() {
        // Preparar
        var um = new NumeroDeCartao(NUMERO);
        var outro = new NumeroDeCartao(NUMERO);

        // Verificar
        assertThat(um).isEqualTo(outro).hasSameHashCodeAs(outro);
    }
}
