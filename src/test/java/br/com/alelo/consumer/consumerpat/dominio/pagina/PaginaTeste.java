package br.com.alelo.consumer.consumerpat.dominio.pagina;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaginaTeste {

    @ParameterizedTest
    @CsvSource({ "0, 20, 0", "1, 20, 1", "20, 20, 1", "21, 20, 2", "50000, 20, 2500",
            "50001, 20, 2501", "100, 100, 1", "101, 100, 2" })
    void deve_arredondar_para_cima_quando_calcular_o_total_de_paginas(long total, int tamanho,
            long esperado) {
        // Agir
        var pagina = new Pagina<String>(List.of(), 1, tamanho, total);

        // Verificar
        assertThat(pagina.totalPaginas()).isEqualTo(esperado);
    }

    @Test
    void deve_guardar_uma_copia_imutavel_quando_a_lista_original_muda_depois() {
        // Preparar
        var original = new ArrayList<>(List.of("a"));
        var pagina = new Pagina<>(original, 1, 20, 1);

        // Agir
        original.add("b");

        // Verificar
        assertThat(pagina.itens()).containsExactly("a");
        assertThatThrownBy(() -> pagina.itens().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void deve_aceitar_uma_pagina_vazia_quando_ela_esta_alem_da_ultima() {
        // Agir
        var pagina = new Pagina<String>(List.of(), 9, 20, 5);

        // Verificar: o resultado existe, só a página não tem itens
        assertThat(pagina.itens()).isEmpty();
        assertThat(pagina.total()).isEqualTo(5);
        assertThat(pagina.totalPaginas()).isEqualTo(1);
    }

    @Test
    void deve_lancar_quando_faltam_os_itens() {
        // Agir e Verificar
        assertThatThrownBy(() -> new Pagina<String>(null, 1, 20, 0))
                .isInstanceOf(NullPointerException.class);
    }
}
