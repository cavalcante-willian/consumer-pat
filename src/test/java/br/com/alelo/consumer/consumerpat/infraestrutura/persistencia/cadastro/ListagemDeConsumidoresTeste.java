package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cadastro;

import br.com.alelo.consumer.consumerpat.api.configuracao.ConfiguracaoDeAuditoria;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.pagina.DirecaoDeOrdenacao;
import br.com.alelo.consumer.consumerpat.dominio.pagina.OrdenacaoDeConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.pagina.PedidoDePagina;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.ConfiguracaoDeRelogioDeTeste;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao.CartaoRepositorioJpa;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ConsumidorEntidade;
// editorconfig-checker-disable-next-line
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ConsumidorRepositorioJpa;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ConsumidoresDeTeste;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import javax.persistence.EntityManagerFactory;

import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova a listagem contra o H2 real: a página sai do banco, ordenada e desempatada, e custa duas
 * consultas, e nunca uma por consumidor.
 */
@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Import({ ConfiguracaoDeAuditoria.class, ConfiguracaoDeRelogioDeTeste.class })
class ListagemDeConsumidoresTeste {

    @Autowired
    private ConsumidorRepositorioJpa consumidores;

    @Autowired
    private CartaoRepositorioJpa cartoes;

    @Autowired
    private TestEntityManager gerenciador;

    @Autowired
    private EntityManagerFactory fabrica;

    private long ana2;
    private long ana4;

    private ConsumidorAdaptador adaptador() {
        return new ConsumidorAdaptador(consumidores, cartoes);
    }

    private ConsumidorEntidade gravar(String nome) {
        var linha = ConsumidoresDeTeste.linha();
        linha.setNome(nome);
        return gerenciador.persistAndFlush(linha);
    }

    private static PedidoDePagina pedido(int pagina, int tamanho, OrdenacaoDeConsumidor campo,
            DirecaoDeOrdenacao sentido) {
        return new PedidoDePagina(pagina, tamanho, campo, sentido);
    }

    @BeforeEach
    void semear() {
        gravar("Carla");
        ana2 = gravar("Ana").getId();
        gravar("Bruno");
        ana4 = gravar("Ana").getId();
        gravar("Dani");
        gerenciador.clear();
    }

    @Test
    void deve_ordenar_pelo_nome_e_desempatar_pelo_id_quando_dois_tem_o_mesmo_nome() {
        // Agir
        var pagina = adaptador()
                .listar(pedido(1, 10, OrdenacaoDeConsumidor.NOME, DirecaoDeOrdenacao.ASC));

        // Verificar
        assertThat(pagina.itens()).extracting(resumo -> resumo.nome()).containsExactly("Ana", "Ana",
                "Bruno", "Carla", "Dani");
        assertThat(pagina.itens().get(0).id()).isEqualTo(new ConsumidorId(ana2));
        assertThat(pagina.itens().get(1).id()).isEqualTo(new ConsumidorId(ana4));
    }

    @Test
    void deve_inverter_a_ordem_e_o_desempate_quando_a_direcao_e_decrescente() {
        // Agir
        var pagina = adaptador()
                .listar(pedido(1, 10, OrdenacaoDeConsumidor.NOME, DirecaoDeOrdenacao.DESC));

        // Verificar
        assertThat(pagina.itens()).extracting(resumo -> resumo.nome()).containsExactly("Dani",
                "Carla", "Bruno", "Ana", "Ana");
        assertThat(pagina.itens().get(3).id()).isEqualTo(new ConsumidorId(ana4));
    }

    @Test
    void deve_cortar_a_pagina_no_banco_sem_pular_nem_repetir_quando_percorrer_todas() {
        // Agir
        var primeira = adaptador()
                .listar(pedido(1, 2, OrdenacaoDeConsumidor.NOME, DirecaoDeOrdenacao.ASC));
        var segunda = adaptador()
                .listar(pedido(2, 2, OrdenacaoDeConsumidor.NOME, DirecaoDeOrdenacao.ASC));
        var terceira = adaptador()
                .listar(pedido(3, 2, OrdenacaoDeConsumidor.NOME, DirecaoDeOrdenacao.ASC));

        // Verificar
        assertThat(primeira.itens()).extracting(resumo -> resumo.nome()).containsExactly("Ana",
                "Ana");
        assertThat(segunda.itens()).extracting(resumo -> resumo.nome()).containsExactly("Bruno",
                "Carla");
        assertThat(terceira.itens()).extracting(resumo -> resumo.nome()).containsExactly("Dani");
        assertThat(primeira.total()).isEqualTo(5);
        assertThat(primeira.totalPaginas()).isEqualTo(3);
    }

    @Test
    void deve_devolver_pagina_vazia_com_o_total_quando_a_pagina_esta_alem_da_ultima() {
        // Agir
        var pagina = adaptador()
                .listar(pedido(50, 20, OrdenacaoDeConsumidor.NOME, DirecaoDeOrdenacao.ASC));

        // Verificar
        assertThat(pagina.itens()).isEmpty();
        assertThat(pagina.total()).isEqualTo(5);
    }

    @Test
    void deve_ordenar_pelo_id_quando_a_ordenacao_e_id() {
        // Agir
        var pagina = adaptador()
                .listar(pedido(1, 10, OrdenacaoDeConsumidor.ID, DirecaoDeOrdenacao.DESC));

        // Verificar
        assertThat(pagina.itens()).extracting(resumo -> resumo.id().valor())
                .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    void deve_fazer_so_duas_consultas_quando_listar_nao_uma_por_consumidor() {
        // Preparar: o contador de comandos de verdade, do Hibernate
        var estatisticas = fabrica.unwrap(SessionFactory.class).getStatistics();
        estatisticas.clear();

        // Agir
        adaptador().listar(pedido(1, 2, OrdenacaoDeConsumidor.NOME, DirecaoDeOrdenacao.ASC));

        // Verificar: a página e a contagem, e nada por consumidor
        assertThat(estatisticas.getPrepareStatementCount()).isEqualTo(2);
    }

    @Test
    void deve_dispensar_a_contagem_quando_a_primeira_pagina_nao_esta_cheia() {
        // Preparar
        var estatisticas = fabrica.unwrap(SessionFactory.class).getStatistics();
        estatisticas.clear();

        // Agir: 5 consumidores numa página de 10, o total já é conhecido
        var pagina = adaptador()
                .listar(pedido(1, 10, OrdenacaoDeConsumidor.NOME, DirecaoDeOrdenacao.ASC));

        // Verificar
        assertThat(estatisticas.getPrepareStatementCount()).isEqualTo(1);
        assertThat(pagina.total()).isEqualTo(5);
    }

    @Test
    void deve_devolver_so_o_id_e_o_nome_quando_listar() {
        // Agir
        var pagina = adaptador()
                .listar(pedido(1, 1, OrdenacaoDeConsumidor.ID, DirecaoDeOrdenacao.ASC));

        // Verificar
        assertThat(pagina.itens()).hasSize(1);
        assertThat(pagina.itens().get(0).nome()).isEqualTo("Carla");
    }
}
