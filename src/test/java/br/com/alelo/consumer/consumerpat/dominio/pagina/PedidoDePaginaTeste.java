package br.com.alelo.consumer.consumerpat.dominio.pagina;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PedidoDePaginaTeste {

    private static PedidoDePagina pedido(int pagina, int tamanho) {
        return new PedidoDePagina(pagina, tamanho, OrdenacaoDeConsumidor.NOME,
                DirecaoDeOrdenacao.ASC);
    }

    @Test
    void deve_ter_o_teto_de_cem_e_o_padrao_de_vinte_quando_consultar_as_constantes() {
        // Verificar
        assertThat(PedidoDePagina.TAMANHO_MAXIMO).isEqualTo(100);
        assertThat(PedidoDePagina.TAMANHO_PADRAO).isEqualTo(20);
    }

    @ParameterizedTest
    @CsvSource({ "0, 20", "-1, 20", "1, 0", "1, 101" })
    void deve_lancar_quando_a_pagina_ou_o_tamanho_estao_fora_dos_limites(int pagina, int tamanho) {
        // Agir e Verificar
        assertThatThrownBy(() -> pedido(pagina, tamanho))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_lancar_quando_falta_a_ordenacao_ou_a_direcao() {
        // Agir e Verificar
        assertThatThrownBy(() -> new PedidoDePagina(1, 20, null, DirecaoDeOrdenacao.ASC))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new PedidoDePagina(1, 20, OrdenacaoDeConsumidor.ID, null))
                .isInstanceOf(NullPointerException.class);
    }
}
