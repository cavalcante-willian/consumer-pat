package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.listar;

import br.com.alelo.consumer.consumerpat.aplicacao.fakes.consumidor.ConsumidorPortaFake;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.CartoesDoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.DadosDoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ResumoDeConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.pagina.DirecaoDeOrdenacao;
import br.com.alelo.consumer.consumerpat.dominio.pagina.OrdenacaoDeConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.pagina.Pagina;
import br.com.alelo.consumer.consumerpat.dominio.pagina.PedidoDePagina;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeConsumidores;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ListarConsumidoresCasoTeste {

    private static Consumidor consumidor(long id, String nome) {
        var base = FabricaDeConsumidores.dadosComDocumento(String.format("%011d", id));
        var dados = new DadosDoConsumidor(nome, base.documento(), base.dataNascimento(),
                base.contato(), base.endereco());
        return new Consumidor(new ConsumidorId(id), dados, new CartoesDoConsumidor(List.of()), 0);
    }

    private static ConsumidorPortaFake porta() {
        return new ConsumidorPortaFake(consumidor(1, "Carla"), consumidor(2, "Ana"),
                consumidor(3, "Bruno"), consumidor(4, "Ana"), consumidor(5, "Dani"));
    }

    private static PedidoDePagina pedido(int pagina, int tamanho, OrdenacaoDeConsumidor campo,
            DirecaoDeOrdenacao sentido) {
        return new PedidoDePagina(pagina, tamanho, campo, sentido);
    }

    private static List<Long> ids(Pagina<ResumoDeConsumidor> pagina) {
        return pagina.itens().stream().map(resumo -> resumo.id().valor()).toList();
    }

    @Test
    void deve_ordenar_pelo_nome_e_desempatar_pelo_id_quando_dois_consumidores_tem_o_mesmo_nome() {
        // Agir
        var pagina = new ListarConsumidoresCaso(porta())
                .listar(pedido(1, 10, OrdenacaoDeConsumidor.NOME, DirecaoDeOrdenacao.ASC));

        // Verificar: Ana (2) antes de Ana (4), por causa do desempate
        assertThat(ids(pagina)).containsExactly(2L, 4L, 3L, 1L, 5L);
    }

    @Test
    void deve_inverter_tudo_inclusive_o_desempate_quando_a_direcao_e_decrescente() {
        // Agir
        var pagina = new ListarConsumidoresCaso(porta())
                .listar(pedido(1, 10, OrdenacaoDeConsumidor.NOME, DirecaoDeOrdenacao.DESC));

        // Verificar
        assertThat(ids(pagina)).containsExactly(5L, 1L, 3L, 4L, 2L);
    }

    @Test
    void deve_ordenar_pelo_id_quando_a_ordenacao_e_id() {
        // Agir
        var pagina = new ListarConsumidoresCaso(porta())
                .listar(pedido(1, 10, OrdenacaoDeConsumidor.ID, DirecaoDeOrdenacao.DESC));

        // Verificar
        assertThat(ids(pagina)).containsExactly(5L, 4L, 3L, 2L, 1L);
    }

    @Test
    void deve_cortar_a_pagina_pedida_sem_pular_nem_repetir_quando_percorrer_todas() {
        // Preparar
        var caso = new ListarConsumidoresCaso(porta());

        // Agir
        var primeira = caso
                .listar(pedido(1, 2, OrdenacaoDeConsumidor.NOME, DirecaoDeOrdenacao.ASC));
        var segunda = caso.listar(pedido(2, 2, OrdenacaoDeConsumidor.NOME, DirecaoDeOrdenacao.ASC));
        var terceira = caso
                .listar(pedido(3, 2, OrdenacaoDeConsumidor.NOME, DirecaoDeOrdenacao.ASC));

        // Verificar: as três páginas juntas são o resultado todo, uma vez cada
        assertThat(ids(primeira)).containsExactly(2L, 4L);
        assertThat(ids(segunda)).containsExactly(3L, 1L);
        assertThat(ids(terceira)).containsExactly(5L);
        assertThat(primeira.total()).isEqualTo(5);
        assertThat(primeira.totalPaginas()).isEqualTo(3);
    }

    @Test
    void deve_devolver_pagina_vazia_com_o_total_quando_a_pagina_esta_alem_da_ultima() {
        // Agir
        var pagina = new ListarConsumidoresCaso(porta())
                .listar(pedido(9, 20, OrdenacaoDeConsumidor.NOME, DirecaoDeOrdenacao.ASC));

        // Verificar
        assertThat(pagina.itens()).isEmpty();
        assertThat(pagina.total()).isEqualTo(5);
        assertThat(pagina.pagina()).isEqualTo(9);
    }

    @Test
    void deve_devolver_so_o_id_e_o_nome_quando_listar() {
        // Agir
        var pagina = new ListarConsumidoresCaso(porta())
                .listar(pedido(1, 1, OrdenacaoDeConsumidor.ID, DirecaoDeOrdenacao.ASC));

        // Verificar
        assertThat(pagina.itens())
                .containsExactly(new ResumoDeConsumidor(new ConsumidorId(1), "Carla"));
    }

    @Test
    void deve_devolver_vazio_quando_nao_ha_consumidor() {
        // Agir
        var pagina = new ListarConsumidoresCaso(new ConsumidorPortaFake())
                .listar(pedido(1, 20, OrdenacaoDeConsumidor.NOME, DirecaoDeOrdenacao.ASC));

        // Verificar
        assertThat(pagina.itens()).isEmpty();
        assertThat(pagina.totalPaginas()).isZero();
    }

    @Test
    void deve_rodar_numa_transacao_so_de_leitura_quando_consultar_a_anotacao_do_metodo()
            throws Exception {
        // Agir
        var metodo = ListarConsumidoresCaso.class.getMethod("listar", PedidoDePagina.class);

        // Verificar
        var transacao = metodo.getAnnotation(Transactional.class);
        assertThat(transacao).isNotNull();
        assertThat(transacao.readOnly()).isTrue();
    }
}
