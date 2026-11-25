package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cadastro;

import br.com.alelo.consumer.consumerpat.dominio.pagina.DirecaoDeOrdenacao;
import br.com.alelo.consumer.consumerpat.dominio.pagina.OrdenacaoDeConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.pagina.PedidoDePagina;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class OrdenacaoNoBancoTeste {

    @Test
    void deve_converter_a_pagina_de_base_um_para_base_zero_quando_montar_o_pedido_do_banco() {
        // Agir
        var pagina = OrdenacaoNoBanco
                .de(new PedidoDePagina(3, 20, OrdenacaoDeConsumidor.NOME, DirecaoDeOrdenacao.ASC));

        // Verificar
        assertThat(pagina.getPageNumber()).isEqualTo(2);
        assertThat(pagina.getPageSize()).isEqualTo(20);
    }

    @Test
    void deve_ordenar_pelo_nome_e_desempatar_pelo_id_quando_a_ordenacao_e_nome() {
        // Agir
        var ordens = OrdenacaoNoBanco
                .de(new PedidoDePagina(1, 20, OrdenacaoDeConsumidor.NOME, DirecaoDeOrdenacao.DESC))
                .getSort().toList();

        // Verificar
        assertThat(ordens).extracting(Sort.Order::getProperty).containsExactly("nome", "id");
        assertThat(ordens).allMatch(Sort.Order::isDescending);
    }

    @Test
    void deve_ordenar_so_pelo_id_sem_repeti_lo_quando_a_ordenacao_e_id() {
        // Agir
        var ordens = OrdenacaoNoBanco
                .de(new PedidoDePagina(1, 20, OrdenacaoDeConsumidor.ID, DirecaoDeOrdenacao.ASC))
                .getSort().toList();

        // Verificar
        assertThat(ordens).extracting(Sort.Order::getProperty).containsExactly("id");
    }
}
