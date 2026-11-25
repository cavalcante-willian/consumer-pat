package br.com.alelo.consumer.consumerpat.fabricas;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentosDeTesteTeste {

    @Test
    void deve_ter_onze_digitos_quando_pedir_o_proximo() {
        // Agir e Verificar
        assertThat(DocumentosDeTeste.proximo()).matches("\\d{11}");
    }

    @Test
    void deve_nunca_repetir_um_documento_quando_pedir_muitos() {
        // Agir
        var documentos = Stream.generate(DocumentosDeTeste::proximo).limit(1_000).toList();

        // Verificar
        assertThat(documentos).doesNotHaveDuplicates();
    }

    @Test
    void deve_nunca_repetir_um_documento_quando_pedir_de_varias_threads() {
        // Agir
        var documentos = Stream.generate(DocumentosDeTeste::proximo).parallel().limit(1_000)
                .toList();

        // Verificar
        assertThat(documentos).doesNotHaveDuplicates();
    }
}
