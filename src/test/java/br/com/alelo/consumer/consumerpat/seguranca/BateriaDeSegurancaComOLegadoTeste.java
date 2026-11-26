package br.com.alelo.consumer.consumerpat.seguranca;

import br.com.alelo.consumer.consumerpat.medicao.AmbienteDeMedicao;
import br.com.alelo.consumer.consumerpat.medicao.ClienteHttp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Roda a bateria de segurança contra o legado e o novo, lado a lado, e escreve o relatório.
 *
 * <p>
 * Os dois sistemas precisam estar de pé em processos separados, e quem os sobe é o roteiro
 * {@code scripts/medir-contra-o-original.sh}, no modo {@code seguranca}, que também extrai o
 * original do histórico. Só roda com {@code SEGURANCA=1}, porque depende desses processos. Os
 * endereços vêm de {@code URL_LEGADO} e {@code URL_NOVO}.
 *
 * <p>
 * Só localhost e só os nossos processos, e nada destrutivo: a verificação de taxa são duzentas
 * requisições de leitura, em série. O resultado vai para {@code build/medicao/seguranca.txt}.
 */
@EnabledIfEnvironmentVariable(named = "SEGURANCA", matches = "1")
class BateriaDeSegurancaComOLegadoTeste {

    private static final Logger LOG = LoggerFactory
            .getLogger(BateriaDeSegurancaComOLegadoTeste.class);

    private static String ambiente(String nome) {
        return Optional.ofNullable(System.getenv(nome))
                .orElseThrow(() -> new IllegalStateException("Faltou a variável " + nome));
    }

    @Test
    void deve_registrar_o_comparativo_de_seguranca_quando_os_dois_sistemas_estao_de_pe()
            throws Exception {
        // Agir
        var legado = BateriaDeSeguranca
                .executar(new SondaDoLegado(new ClienteHttp(ambiente("URL_LEGADO"))));
        var novo = BateriaDeSeguranca
                .executar(new SondaDoNovo(new ClienteHttp(ambiente("URL_NOVO"))));

        // Verificar: o relatório é o resultado, e nenhum veredito é afirmado aqui
        var relatorio = new ArrayList<String>();
        relatorio.add("Bateria de segurança do legado e do sistema novo");
        relatorio.add("Medido em " + Instant.now());
        relatorio.add("");
        relatorio.add("Ambiente:");
        AmbienteDeMedicao.descricao().forEach(linha -> relatorio.add("  " + linha));
        relatorio.add("");
        relatorio.add(
                "Só localhost, só os processos acima, nada destrutivo. Cada verificação é uma");
        relatorio.add("tentativa, e passar nela não prova ausência de falha.");
        relatorio.add("");
        relatorio.addAll(RelatorioDeSeguranca.montar(legado, novo));
        var arquivo = Path.of("build", "medicao", "seguranca.txt");
        Files.createDirectories(arquivo.getParent());
        Files.write(arquivo, relatorio);
        relatorio.forEach(LOG::info);
        assertThat(arquivo).isNotEmptyFile();
    }
}
