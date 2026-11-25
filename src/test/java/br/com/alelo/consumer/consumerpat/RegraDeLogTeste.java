package br.com.alelo.consumer.consumerpat;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Impõe a regra do log: só {@code infraestrutura.log} fala com uma biblioteca de log, e nada usa a
 * saída padrão. A porta é o único caminho, e é por ela que o dado sensível é mascarado.
 *
 * <p>
 * Vale para os pacotes da arquitetura nova. O código do legado, que a remoção do legado apaga, usa
 * {@code @Slf4j} do Lombok e fica fora desta varredura.
 */
class RegraDeLogTeste {

    private static final Path RAIZ = Path.of("src/main/java/br/com/alelo/consumer/consumerpat");

    private static final List<String> PACOTES = List.of("dominio", "aplicacao", "api",
            "infraestrutura");

    private static final String DO_LOG = "infraestrutura/log/";

    private static final List<String> PROIBIDOS_NOS_IMPORTS = List.of("org.slf4j.",
            "ch.qos.logback.", "org.apache.logging.", "org.apache.commons.logging.",
            "java.util.logging.", "lombok.extern.");

    private static final List<String> PROIBIDOS_NO_TEXTO = List.of("System.out", "System.err",
            "printStackTrace");

    private static List<Path> fontes() throws IOException {
        return PACOTES.stream().flatMap(pacote -> {
            try (Stream<Path> arquivos = Files.walk(RAIZ.resolve(pacote))) {
                return arquivos.filter(arquivo -> arquivo.toString().endsWith(".java")).toList()
                        .stream();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).toList();
    }

    private static List<String> linhasDe(Path fonte) {
        try {
            return Files.readAllLines(fonte);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean ehDoLog(Path fonte) {
        return fonte.toString().replace('\\', '/').contains(DO_LOG);
    }

    @Test
    void deve_encontrar_fontes_nos_pacotes_da_arquitetura_nova() throws IOException {
        // Agir
        var fontes = fontes();

        // Verificar
        assertThat(fontes).isNotEmpty();
        assertThat(fontes).anyMatch(RegraDeLogTeste::ehDoLog);
    }

    @Test
    void deve_importar_biblioteca_de_log_so_em_infraestrutura_log() throws IOException {
        // Preparar
        var imports = fontes().stream().filter(fonte -> !ehDoLog(fonte))
                .flatMap(fonte -> linhasDe(fonte).stream()
                        .filter(linha -> linha.startsWith("import "))
                        .map(linha -> fonte.getFileName() + ": " + linha))
                .toList();

        // Agir
        var proibidos = imports.stream().filter(linha -> PROIBIDOS_NOS_IMPORTS.stream()
                .anyMatch(prefixo -> linha.contains(prefixo))).toList();

        // Verificar
        assertThat(proibidos).isEmpty();
    }

    @Test
    void deve_importar_o_slf4j_em_infraestrutura_log_quando_varrer_o_adaptador()
            throws IOException {
        // Agir
        var importam = fontes().stream().filter(RegraDeLogTeste::ehDoLog)
                .filter(fonte -> linhasDe(fonte).stream()
                        .anyMatch(linha -> linha.startsWith("import org.slf4j.")))
                .toList();

        // Verificar: a regra existe porque o adaptador usa mesmo o SLF4J
        assertThat(importam).isNotEmpty();
    }

    @Test
    void deve_nao_usar_a_saida_padrao_nem_imprimir_pilha_quando_varrer_o_codigo_de_producao()
            throws IOException {
        // Agir
        var usos = fontes().stream().flatMap(fonte -> linhasDe(fonte).stream()
                .filter(linha -> !linha.strip().startsWith("*") && !linha.strip().startsWith("//"))
                .filter(linha -> PROIBIDOS_NO_TEXTO.stream().anyMatch(linha::contains))
                .map(linha -> fonte.getFileName() + ": " + linha.strip())).toList();

        // Verificar
        assertThat(usos).isEmpty();
    }
}
