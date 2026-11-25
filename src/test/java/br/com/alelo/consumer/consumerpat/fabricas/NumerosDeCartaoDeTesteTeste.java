package br.com.alelo.consumer.consumerpat.fabricas;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class NumerosDeCartaoDeTesteTeste {

    @Test
    void deve_ter_dezesseis_digitos_quando_pedir_o_proximo() {
        // Agir e Verificar
        assertThat(NumerosDeCartaoDeTeste.proximo()).matches("\\d{16}");
    }

    @Test
    void deve_diferir_do_numero_padrao_da_fabrica_quando_pedir_o_proximo() {
        // Agir e Verificar
        assertThat(NumerosDeCartaoDeTeste.proximo()).isNotEqualTo(FabricaDeCartoes.NUMERO_PADRAO);
    }

    @Test
    void deve_nunca_repetir_um_numero_quando_pedir_de_varias_threads() {
        // Agir
        var numeros = Stream.generate(NumerosDeCartaoDeTeste::proximo).parallel().limit(1_000)
                .toList();

        // Verificar
        assertThat(numeros).doesNotHaveDuplicates();
    }
}
