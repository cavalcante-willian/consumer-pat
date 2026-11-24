package br.com.alelo.consumer.consumerpat.api.comum.problema;

import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class TituloDaCategoriaTeste {

    @ParameterizedTest
    @EnumSource(CategoriaErro.class)
    void deve_ter_titulo_quando_consultar_qualquer_categoria(CategoriaErro categoria) {
        // Agir
        var titulo = TituloDaCategoria.de(categoria);

        // Verificar
        assertThat(titulo).isNotBlank();
    }

    @ParameterizedTest
    @EnumSource(CategoriaErro.class)
    void deve_ter_titulo_proprio_quando_comparar_com_as_outras_categorias(CategoriaErro categoria) {
        // Agir
        var tituloDasOutras = Arrays.stream(CategoriaErro.values())
                .filter(outra -> outra != categoria).map(TituloDaCategoria::de).toList();

        // Verificar
        assertThat(tituloDasOutras).doesNotContain(TituloDaCategoria.de(categoria));
    }
}
