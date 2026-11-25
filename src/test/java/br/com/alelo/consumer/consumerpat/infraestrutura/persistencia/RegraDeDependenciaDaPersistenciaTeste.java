package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Impõe, por teste, as duas regras que o compilador deixou de impor quando a persistência se
 * dividiu em pacotes por conceito.
 *
 * <p>
 * A primeira: as entidades e os repositórios são públicos porque as chaves estrangeiras os fazem se
 * enxergar entre pacotes, mas nada fora de {@code infraestrutura.persistencia} pode importá-los,
 * senão a JPA vazaria para o resto do sistema. A segunda: os pacotes da persistência dependem uns
 * dos outros em um só sentido, o do consumidor para o extrato, e um ciclo entre eles é recusado.
 * Como o projeto é um módulo único, nenhuma das duas é do compilador, e este teste as impõe lendo
 * os imports dos fontes.
 */
class RegraDeDependenciaDaPersistenciaTeste {

    private static final String BASE = "br.com.alelo.consumer.consumerpat";

    private static final String PERSISTENCIA = BASE + ".infraestrutura.persistencia";

    private static final Path FONTES = Path.of("src/main/java/br/com/alelo/consumer/consumerpat");

    /**
     * De quem cada pacote da persistência pode depender, dentro da própria persistência. O
     * consumidor é a folha; o cartão depende dele pela chave estrangeira; o extrato, do cartão; e o
     * cadastro compõe o consumidor e o cartão.
     */
    private static final Map<String, List<String>> PERMITIDOS_POR_PACOTE = Map.of("consumidor",
            List.of(), "cartao", List.of("consumidor"), "extrato", List.of("cartao"), "cadastro",
            List.of("consumidor", "cartao"));

    private static List<Path> fontes() throws IOException {
        try (Stream<Path> arquivos = Files.walk(FONTES)) {
            return arquivos.filter(arquivo -> arquivo.toString().endsWith(".java")).toList();
        }
    }

    private static List<String> importsDe(Path fonte) {
        try {
            return Files.readAllLines(fonte).stream().filter(linha -> linha.startsWith("import "))
                    .map(linha -> linha.replaceFirst("^import (static )?", "")
                            .replaceFirst(";.*$", "").strip())
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean dentroDaPersistencia(Path fonte) {
        return fonte.toString().contains("/infraestrutura/persistencia/");
    }

    private static boolean ehEntidadeOuRepositorio(String importado) {
        return importado.startsWith(PERSISTENCIA + ".")
                && (importado.endsWith("Entidade") || importado.endsWith("RepositorioJpa"));
    }

    @Test
    void deve_encontrar_fontes_da_persistencia_quando_varrer_os_pacotes() throws IOException {
        // Agir
        var dentro = fontes().stream()
                .filter(RegraDeDependenciaDaPersistenciaTeste::dentroDaPersistencia).toList();

        // Verificar
        assertThat(dentro).isNotEmpty();
    }

    @Test
    void deve_nao_importar_entidade_nem_repositorio_quando_o_fonte_esta_fora_da_persistencia()
            throws IOException {
        // Preparar
        var fora = fontes().stream().filter(fonte -> !dentroDaPersistencia(fonte)).toList();

        // Agir
        var violacoes = fora.stream()
                .flatMap(fonte -> importsDe(fonte).stream()
                        .filter(RegraDeDependenciaDaPersistenciaTeste::ehEntidadeOuRepositorio)
                        .map(importado -> fonte.getFileName() + " importa " + importado))
                .toList();

        // Verificar
        assertThat(fora).isNotEmpty();
        assertThat(violacoes).isEmpty();
    }

    @Test
    void deve_depender_so_do_permitido_quando_um_pacote_da_persistencia_importa_outro()
            throws IOException {
        // Agir
        var violacoes = fontes().stream()
                .filter(RegraDeDependenciaDaPersistenciaTeste::dentroDaPersistencia)
                .flatMap(fonte -> PERMITIDOS_POR_PACOTE.entrySet().stream()
                        .filter(pacote -> fonte.toString()
                                .contains("/persistencia/" + pacote.getKey() + "/"))
                        .flatMap(pacote -> importsDe(fonte).stream()
                                .filter(importado -> importado.startsWith(PERSISTENCIA + "."))
                                .filter(importado -> !importado
                                        .startsWith(PERSISTENCIA + "." + pacote.getKey() + "."))
                                .filter(importado -> pacote.getValue().stream()
                                        .noneMatch(permitido -> importado
                                                .startsWith(PERSISTENCIA + "." + permitido + ".")))
                                .map(importado -> pacote.getKey() + ": " + fonte.getFileName()
                                        + " importa " + importado)))
                .toList();

        // Verificar
        assertThat(violacoes).isEmpty();
    }

    @Test
    void deve_cobrir_todos_os_pacotes_quando_listar_as_pastas_da_persistencia() throws IOException {
        // Agir
        try (Stream<Path> pastas = Files.list(FONTES.resolve("infraestrutura/persistencia"))) {
            var nomes = pastas.filter(Files::isDirectory)
                    .map(pasta -> pasta.getFileName().toString()).toList();

            // Verificar: um pacote novo sem regra aqui é um pacote sem fronteira
            assertThat(nomes).containsExactlyInAnyOrderElementsOf(PERMITIDOS_POR_PACOTE.keySet());
        }
    }
}
