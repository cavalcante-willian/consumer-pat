package br.com.alelo.consumer.consumerpat.medicao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mede o legado e o novo, lado a lado, em HTTP de verdade, e escreve o comparativo.
 *
 * <p>
 * Não é teste de regra: nenhum tempo é afirmado, porque o tempo depende da máquina. Os dois
 * sistemas precisam estar de pé em processos separados, e quem os sobe é o roteiro
 * {@code tools/medir-contra-o-original.sh}, que também extrai o original do histórico. Fica
 * desligado por padrão, por levar minutos, e só roda com {@code MEDICAO=1}. O endereço de cada
 * sistema vem de {@code URL_LEGADO} e {@code URL_NOVO}, e a quantidade de consumidores, de
 * {@code QUANTIDADE}, que por padrão é a da linha de base, cinquenta mil.
 *
 * <p>
 * O resultado vai para {@code build/medicao/comparativo.txt}. Vale o relativo, e não o absoluto: o
 * H2 em memória não tem latência de rede nem de disco, e a máquina de medição não é a de produção.
 */
@EnabledIfEnvironmentVariable(named = "MEDICAO", matches = "1")
class ComparativoComOLegadoTeste {

    private static final Logger LOG = LoggerFactory.getLogger(ComparativoComOLegadoTeste.class);

    private static final int QUANTIDADE_PADRAO = 50_000;

    private static String ambiente(String nome) {
        return Optional.ofNullable(System.getenv(nome))
                .orElseThrow(() -> new IllegalStateException("Faltou a variável " + nome));
    }

    private static List<Medida> medir(OperacoesDoAlvo alvo, List<Cenario> cenarios)
            throws Exception {
        var medidas = new ArrayList<Medida>();
        for (var cenario : cenarios) {
            medidas.addAll(cenario.medir(alvo));
        }
        return medidas;
    }

    @Test
    void deve_registrar_o_comparativo_quando_os_dois_sistemas_estao_de_pe() throws Exception {
        // Preparar
        var total = Optional.ofNullable(System.getenv("QUANTIDADE")).map(Integer::parseInt)
                .orElse(QUANTIDADE_PADRAO);
        var legado = new OperacoesDoLegado(new ClienteHttp(ambiente("URL_LEGADO")));
        var novo = new OperacoesDoNovo(new ClienteHttp(ambiente("URL_NOVO")));
        // A disputa vai antes da semeadura, e o crédito antes da compra
        List<Cenario> cenarios = List.of(new CenarioDeConcorrencia(), new CenarioDeCadastro(total),
                new CenarioDeListagem(total), new CenarioDeCredito(total),
                new CenarioDeCompra(total), new CenarioDeConsultaPorId(total));

        // Agir
        var medidasDoLegado = medir(legado, cenarios);
        var medidasDoNovo = medir(novo, cenarios);

        // Verificar: o relatório, que é o resultado, e não um tempo afirmado
        var relatorio = new ArrayList<String>();
        relatorio.add("Comparativo do legado com o sistema novo — " + total + " consumidores");
        relatorio.add("Medido em " + Instant.now());
        relatorio.add("");
        relatorio.add(
                "Ambiente (os tempos absolutos valem só nele; vale a razão entre os sistemas):");
        AmbienteDeMedicao.descricao().forEach(linha -> relatorio.add("  " + linha));
        relatorio.add("");
        relatorio.add("HTTP real em dois processos, H2 em memória, mesma máquina, um sistema medido"
                + " por vez");
        relatorio.add("Aquecimento: " + MedicaoDePedidos.AQUECIMENTO + " chamadas descartadas;"
                + " medições: " + MedicaoDePedidos.MEDICOES + " por chamada");
        relatorio.add("O novo registra uma linha de log por requisição (item 10); o legado não"
                + " registra nada");
        relatorio.add("");
        relatorio.addAll(RelatorioComparativo.montar(medidasDoLegado, medidasDoNovo));
        var arquivo = Path.of("build", "medicao", "comparativo.txt");
        Files.createDirectories(arquivo.getParent());
        Files.write(arquivo, relatorio);
        relatorio.forEach(LOG::info);
        assertThat(arquivo).isNotEmptyFile();
    }
}
