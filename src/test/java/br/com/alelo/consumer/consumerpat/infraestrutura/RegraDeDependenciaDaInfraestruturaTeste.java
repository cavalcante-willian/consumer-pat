package br.com.alelo.consumer.consumerpat.infraestrutura;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Impõe a regra de dependência da infraestrutura: ela depende da biblioteca padrão, do domínio, da
 * aplicação, de si mesma e das ferramentas de persistência. Nunca da API: a dependência aponta para
 * o domínio, e não para quem monta a aplicação. Como o projeto é um módulo único, o compilador não
 * impede um {@code import} da API; este teste o impede lendo os imports dos fontes.
 */
class RegraDeDependenciaDaInfraestruturaTeste {

    private static final Path INFRAESTRUTURA = Path
            .of("src/main/java/br/com/alelo/consumer/consumerpat/infraestrutura");

    private static final List<String> PERMITIDOS = List.of("java.", "javax.persistence.", "lombok.",
            "org.hibernate.", "org.springframework.data.", "com.zaxxer.hikari.",
            "br.com.alelo.consumer.consumerpat.dominio.",
            "br.com.alelo.consumer.consumerpat.aplicacao.",
            "br.com.alelo.consumer.consumerpat.infraestrutura.");

    private static List<Path> fontesDaInfraestrutura() throws IOException {
        try (Stream<Path> arquivos = Files.walk(INFRAESTRUTURA)) {
            return arquivos.filter(arquivo -> arquivo.toString().endsWith(".java")).toList();
        }
    }

    private static List<String> importsDe(Path fonte) {
        try {
            return Files.readAllLines(fonte).stream().filter(linha -> linha.startsWith("import "))
                    .map(linha -> linha.replaceFirst("^import (static )?", "").replace(";", "")
                            .strip())
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void deve_encontrar_fontes_quando_varrer_a_infraestrutura() throws IOException {
        // Agir
        var fontes = fontesDaInfraestrutura();

        // Verificar
        assertThat(fontes).isNotEmpty();
    }

    @Test
    void deve_importar_so_o_permitido_quando_varrer_a_infraestrutura() throws IOException {
        // Preparar
        var imports = fontesDaInfraestrutura().stream().flatMap(fonte -> importsDe(fonte).stream())
                .toList();

        // Agir
        var proibidos = imports.stream()
                .filter(importado -> PERMITIDOS.stream().noneMatch(importado::startsWith)).toList();

        // Verificar
        assertThat(imports).isNotEmpty();
        assertThat(proibidos).isEmpty();
    }
}
