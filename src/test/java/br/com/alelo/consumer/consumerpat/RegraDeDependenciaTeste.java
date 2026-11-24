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
 * Impõe a regra de dependência: o domínio depende só da biblioteca padrão e de
 * si mesmo. Como o
 * projeto é um módulo único, o compilador não impede um {@code import} de
 * Spring, JPA ou de outra
 * camada dentro do domínio; este teste o impede lendo os imports dos fontes.
 */
class RegraDeDependenciaTeste {

    private static final Path DOMINIO = Path.of("src/main/java/br/com/alelo/consumer/consumerpat/dominio");

    private static final List<String> PERMITIDOS = List.of("java.", "br.com.alelo.consumer.consumerpat.dominio.");

    private static List<Path> fontesDoDominio() throws IOException {
        try (Stream<Path> arquivos = Files.walk(DOMINIO)) {
            return arquivos.filter(arquivo -> arquivo.toString().endsWith(".java")).toList();
        }
    }

    private static List<String> importsDe(Path fonte) {
        try {
            return Files.readAllLines(fonte).stream()
                    .filter(linha -> linha.startsWith("import "))
                    .map(linha -> linha.replaceFirst("^import (static )?", "").replace(";", "").strip())
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void deve_encontrar_fontes_quando_varrer_o_dominio() throws IOException {
        // Agir
        var fontes = fontesDoDominio();

        // Verificar
        assertThat(fontes).isNotEmpty();
    }

    @Test
    void deve_importar_so_a_biblioteca_padrao_e_o_proprio_dominio_quando_varrer_o_dominio() throws IOException {
        // Preparar
        var imports = fontesDoDominio().stream().flatMap(fonte -> importsDe(fonte).stream()).toList();

        // Agir
        var proibidos = imports.stream()
                .filter(importado -> PERMITIDOS.stream().noneMatch(importado::startsWith))
                .toList();

        // Verificar
        assertThat(imports).isNotEmpty();
        assertThat(proibidos).isEmpty();
    }
}
