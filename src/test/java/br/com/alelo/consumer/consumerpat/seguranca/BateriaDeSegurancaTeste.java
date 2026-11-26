package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BateriaDeSegurancaTeste {

    @Test
    void deve_ter_dezesseis_verificacoes_com_chaves_diferentes_quando_listar() {
        // Agir
        var chaves = BateriaDeSeguranca.verificacoes().stream().map(Verificacao::chave).toList();

        // Verificar: a chave é o que o relatório usa para juntar o legado e o novo
        assertThat(chaves).hasSize(16).doesNotHaveDuplicates();
    }

    @Test
    void deve_cobrir_o_dinheiro_a_entrada_o_vazamento_e_a_superficie_quando_listar() {
        // Agir
        var chaves = BateriaDeSeguranca.verificacoes().stream().map(Verificacao::chave).toList();

        // Verificar
        assertThat(chaves).contains("valor-negativo", "saldo-por-atualizacao",
                "estado-alterado-por-get", "campo-desconhecido", "entrada-hostil-nao-vira-sql",
                "erro-sem-detalhe-interno", "dado-sensivel-na-resposta", "cabecalhos-de-seguranca",
                "trace-sem-eco", "cors-fechado", "console-do-h2", "documentacao-publica",
                "limite-de-taxa");
    }

    @Test
    void deve_dar_um_achado_por_verificacao_com_a_mesma_chave_na_mesma_ordem_quando_executar()
            throws Exception {
        // Agir
        var achados = BateriaDeSeguranca.executar(new SondaFalsa(true));

        // Verificar
        assertThat(achados).extracting(Achado::chave).containsExactlyElementsOf(
                BateriaDeSeguranca.verificacoes().stream().map(Verificacao::chave).toList());
    }

    @Test
    void deve_proteger_o_sistema_que_se_defende_e_expor_o_que_nao_se_defende_quando_executar()
            throws Exception {
        // Agir
        var seDefende = BateriaDeSeguranca.executar(new SondaFalsa(true));
        var naoSeDefende = BateriaDeSeguranca.executar(new SondaFalsa(false));

        // Verificar: o dinheiro é o que a sonda de mentira estraga, e o limite de taxa é aceito
        assertThat(seDefende).filteredOn(achado -> !achado.protegido()).extracting(Achado::chave)
                .containsExactly("limite-de-taxa");
        assertThat(naoSeDefende).filteredOn(achado -> !achado.protegido()).extracting(Achado::chave)
                .contains("valor-negativo", "valor-enorme");
    }
}
