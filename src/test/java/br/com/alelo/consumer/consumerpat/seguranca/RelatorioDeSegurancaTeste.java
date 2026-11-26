package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RelatorioDeSegurancaTeste {

    private static Achado achado(String chave, boolean protegido, String evidencia) {
        return new Achado(chave, "descrição de " + chave, protegido, evidencia);
    }

    @Test
    void deve_juntar_o_legado_e_o_novo_na_mesma_verificacao_quando_montar() {
        // Agir
        var linhas = RelatorioDeSeguranca.montar(
                List.of(achado("valor-negativo", false, "saldo de 100 para 150")),
                List.of(achado("valor-negativo", true, "status 400")));

        // Verificar
        assertThat(linhas).containsSequence("valor-negativo — descrição de valor-negativo",
                "  legado: EXPOSTO   — saldo de 100 para 150", "  novo  : PROTEGIDO — status 400",
                "");
    }

    @Test
    void deve_contar_em_quantas_verificacoes_cada_sistema_se_protegeu_quando_montar() {
        // Agir
        var linhas = RelatorioDeSeguranca.montar(
                List.of(achado("a", false, "x"), achado("b", true, "y"), achado("c", false, "z")),
                List.of(achado("a", true, "x"), achado("b", true, "y"), achado("c", true, "z")));

        // Verificar
        assertThat(linhas).last()
                .isEqualTo("Protegido em 1 de 3 verificações no legado, e em 3 " + "de 3 no novo.");
    }

    @Test
    void deve_mostrar_um_traco_quando_um_dos_lados_nao_fez_a_verificacao() {
        // Agir
        var linhas = RelatorioDeSeguranca.montar(List.of(),
                List.of(achado("so-no-novo", true, "status 400")));

        // Verificar: nada é inventado para o legado
        assertThat(linhas).containsSequence("so-no-novo — descrição de so-no-novo", "  legado: —",
                "  novo  : PROTEGIDO — status 400");
    }

    @Test
    void deve_usar_a_descricao_do_legado_quando_so_ele_fez_a_verificacao() {
        // Agir
        var linhas = RelatorioDeSeguranca.montar(List.of(achado("so-no-legado", false, "x")),
                List.of());

        // Verificar
        assertThat(linhas).contains("so-no-legado — descrição de so-no-legado", "  novo  : —");
    }

    @Test
    void deve_manter_a_ordem_do_novo_e_por_ultimo_o_que_so_o_legado_tem_quando_montar() {
        // Agir
        var linhas = RelatorioDeSeguranca.montar(
                List.of(achado("b", true, "x"), achado("a", true, "x")),
                List.of(achado("c", true, "x"), achado("a", true, "x")));

        // Verificar
        assertThat(linhas).filteredOn(linha -> linha.contains(" — descrição de "))
                .extracting(linha -> linha.substring(0, 1)).containsExactly("c", "a", "b");
    }
}
