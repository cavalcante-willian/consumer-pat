package br.com.alelo.consumer.consumerpat.dominio.pagina;

import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroEntradaInvalida;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class PedidoDePaginaDeParametrosTeste {

    @Test
    void deve_ser_sucesso_com_o_pedido_quando_os_quatro_parametros_sao_validos() {
        // Agir
        var resultado = PedidoDePaginaDeParametros.converter(2, 20, "nome", "asc");

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isEqualTo(
                new PedidoDePagina(2, 20, OrdenacaoDeConsumidor.NOME, DirecaoDeOrdenacao.ASC));
    }

    @ParameterizedTest
    @CsvSource({ "nome, asc", "NOME, ASC", "Nome, Asc", "id, desc", "ID, DESC" })
    void deve_aceitar_maiusculas_e_minusculas_quando_ler_a_ordenacao_e_a_direcao(String campo,
            String sentido) {
        // Agir
        var resultado = PedidoDePaginaDeParametros.converter(1, 20, campo, sentido);

        // Verificar
        assertInstanceOf(Sucesso.class, resultado);
    }

    @ParameterizedTest
    @CsvSource({ "1, 1", "1, 100", "9999, 50" })
    void deve_aceitar_as_bordas_quando_ler_a_pagina_e_o_tamanho(int pagina, int tamanho) {
        // Agir
        var resultado = PedidoDePaginaDeParametros.converter(pagina, tamanho, "nome", "asc");

        // Verificar
        assertInstanceOf(Sucesso.class, resultado);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1, Integer.MIN_VALUE })
    void deve_recusar_a_pagina_quando_ela_nao_comeca_em_um(int pagina) {
        // Agir
        var falha = assertInstanceOf(Falha.class,
                PedidoDePaginaDeParametros.converter(pagina, 20, "nome", "asc"));

        // Verificar
        assertThat(falha.erro()).isInstanceOf(ErroEntradaInvalida.class);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -5, 101, 500, Integer.MAX_VALUE })
    void deve_recusar_o_tamanho_quando_ele_esta_fora_de_um_a_cem(int tamanho) {
        // Agir
        var falha = assertInstanceOf(Falha.class,
                PedidoDePaginaDeParametros.converter(1, tamanho, "nome", "asc"));

        // Verificar: acima do teto é erro, e não um corte silencioso
        assertThat(((ErroEntradaInvalida) falha.erro()).campos()).containsOnlyKeys("tamanhoPagina");
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "senha", "nome;drop table", "1", "cpf" })
    @NullSource
    void deve_recusar_a_ordenacao_quando_ela_nao_e_de_um_campo_aceito(String ordenacao) {
        // Agir
        var falha = assertInstanceOf(Falha.class,
                PedidoDePaginaDeParametros.converter(1, 20, ordenacao, "asc"));

        // Verificar
        assertThat(((ErroEntradaInvalida) falha.erro()).campos()).containsOnlyKeys("ordenacao");
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "cima", "up", "asc;" })
    @NullSource
    void deve_recusar_a_direcao_quando_ela_nao_e_asc_nem_desc(String direcao) {
        // Agir
        var falha = assertInstanceOf(Falha.class,
                PedidoDePaginaDeParametros.converter(1, 20, "nome", direcao));

        // Verificar
        assertThat(((ErroEntradaInvalida) falha.erro()).campos()).containsOnlyKeys("direcao");
    }

    @Test
    void deve_reunir_os_quatro_erros_de_uma_vez_quando_todos_os_parametros_sao_invalidos() {
        // Agir
        var falha = assertInstanceOf(Falha.class,
                PedidoDePaginaDeParametros.converter(0, 500, "xyz", "cima"));

        // Verificar
        assertThat(((ErroEntradaInvalida) falha.erro()).campos()).containsOnlyKeys("direcao",
                "ordenacao", "pagina", "tamanhoPagina");
    }

    @Test
    void deve_nao_devolver_o_texto_recusado_quando_a_ordenacao_e_invalida() {
        // Agir
        var falha = assertInstanceOf(Falha.class,
                PedidoDePaginaDeParametros.converter(1, 20, "segredo-do-cliente", "asc"));

        // Verificar
        assertThat(falha.toString()).doesNotContain("segredo-do-cliente");
    }
}
