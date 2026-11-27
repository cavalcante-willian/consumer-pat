package br.com.alelo.consumer.consumerpat.api.comum.documentacao;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InformacoesDaApiTeste {

    @Test
    void deve_levar_o_titulo_e_a_versao_recebida_quando_montar() {
        // Agir
        var informacoes = InformacoesDaApi.montar("1.2.3");

        // Verificar
        assertThat(informacoes.getTitle()).isEqualTo(InformacoesDaApi.TITULO);
        assertThat(informacoes.getVersion()).isEqualTo("1.2.3");
    }

    @Test
    void deve_dizer_o_que_vale_para_a_api_inteira_quando_montar() {
        // Agir
        var descricao = InformacoesDaApi.montar("1.2.3").getDescription();

        // Verificar: o formato dos erros, o dinheiro como texto e o cabeçalho de rastreio
        assertThat(descricao).contains("application/problem+json").contains("RFC 7807")
                .contains("texto decimal").contains("X-Request-Id");
    }

    @Test
    void deve_nao_ter_contato_nem_licenca_quando_montar() {
        // Agir: a API é um desafio técnico, e não um produto
        var informacoes = InformacoesDaApi.montar("1.2.3");

        // Verificar
        assertThat(informacoes.getContact()).isNull();
        assertThat(informacoes.getLicense()).isNull();
    }
}
