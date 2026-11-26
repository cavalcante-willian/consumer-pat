package br.com.alelo.consumer.consumerpat.medicao;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalDouble;

import static org.assertj.core.api.Assertions.assertThat;

class RelatorioComparativoTeste {

    private static Medida medida(String chave, String descricao, double mediana) {
        return new Medida(chave, descricao, "resumo de " + descricao, OptionalDouble.of(mediana));
    }

    @Test
    void deve_juntar_o_legado_e_o_novo_na_mesma_chave_quando_os_dois_tem_a_chamada() {
        // Agir
        var linhas = RelatorioComparativo.montar(List.of(medida("compra", "compra antiga", 10.0)),
                List.of(medida("compra", "compra nova", 5.0)));

        // Verificar
        assertThat(linhas).containsSequence("compra",
                "  legado: compra antiga\n          resumo de compra antiga",
                "  novo  : compra nova\n          resumo de compra nova",
                "  razão : 2,0× (legado ÷ novo, pela mediana)", "");
    }

    @Test
    void deve_mostrar_a_razao_abaixo_de_um_quando_o_novo_e_mais_lento() {
        // Agir: o preço da trava da disputa
        var linhas = RelatorioComparativo.montar(List.of(medida("disputa", "a", 40.0)),
                List.of(medida("disputa", "b", 100.0)));

        // Verificar
        assertThat(linhas).contains("  razão : 0,4× (legado ÷ novo, pela mediana)");
    }

    @Test
    void deve_mostrar_um_traco_quando_o_legado_nao_tem_a_chamada() {
        // Agir: a consulta por id só existe no novo
        var linhas = RelatorioComparativo.montar(List.of(),
                List.of(medida("consulta-por-id", "consulta", 3.0)));

        // Verificar: nada é inventado para o legado, e sem os dois não há razão
        assertThat(linhas).containsSequence("consulta-por-id", "  legado: —",
                "  novo  : consulta\n          resumo de consulta", "  razão : —");
    }

    @Test
    void deve_mostrar_um_traco_quando_o_novo_nao_tem_a_chamada() {
        // Agir
        var linhas = RelatorioComparativo.montar(List.of(medida("so-legado", "antiga", 3.0)),
                List.of());

        // Verificar
        assertThat(linhas).contains("  novo  : —", "  razão : —");
    }

    @Test
    void deve_mostrar_um_traco_na_razao_quando_a_medida_nao_tem_numero_principal() {
        // Preparar: o saldo final da disputa é texto, e não um tempo
        var semNumero = new Medida("saldo", "saldo", "saldo final", OptionalDouble.empty());

        // Agir
        var linhas = RelatorioComparativo.montar(List.of(semNumero), List.of(semNumero));

        // Verificar
        assertThat(linhas).contains("  razão : —");
    }

    @Test
    void deve_manter_a_ordem_em_que_as_chamadas_apareceram_quando_montar() {
        // Agir: a ordem é a do novo, e a chamada que só o legado tem vem depois
        var linhas = RelatorioComparativo.montar(
                List.of(medida("b", "b", 1.0), medida("a", "a", 1.0)),
                List.of(medida("c", "c", 1.0), medida("a", "a", 1.0)));

        // Verificar
        assertThat(linhas).filteredOn(linha -> List.of("a", "b", "c").contains(linha))
                .containsExactly("c", "a", "b");
    }
}
