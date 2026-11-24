package br.com.alelo.consumer.consumerpat.aplicacao;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Impõe a regra de dependência da aplicação: ela depende da biblioteca padrão, do domínio e de si
 * mesma, e de uma única classe de framework, a anotação de fronteira de transação do Spring. Como o
 * projeto é um módulo único, o compilador não impede um {@code import} de JPA, de web ou da
 * infraestrutura; este teste o impede lendo os imports dos fontes.
 */
class RegraDeDependenciaDaAplicacaoTeste {

    private static final Path APLICACAO = Path
            .of("src/main/java/br/com/alelo/consumer/consumerpat/aplicacao");

    private static final List<String> PERMITIDOS = List.of("java.",
            "br.com.alelo.consumer.consumerpat.dominio.",
            "br.com.alelo.consumer.consumerpat.aplicacao.",
            "org.springframework.transaction.annotation.Transactional");

    private static List<Path> fontesDaAplicacao() throws IOException {
        try (Stream<Path> arquivos = Files.walk(APLICACAO)) {
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
    void deve_encontrar_fontes_quando_varrer_a_aplicacao() throws IOException {
        // Agir
        var fontes = fontesDaAplicacao();

        // Verificar
        assertThat(fontes).isNotEmpty();
    }

    @Test
    void deve_importar_so_o_permitido_quando_varrer_a_aplicacao() throws IOException {
        // Preparar
        var imports = fontesDaAplicacao().stream().flatMap(fonte -> importsDe(fonte).stream())
                .toList();

        // Agir
        var proibidos = imports.stream()
                .filter(importado -> PERMITIDOS.stream().noneMatch(importado::startsWith)).toList();

        // Verificar
        assertThat(imports).isNotEmpty();
        assertThat(proibidos).isEmpty();
    }
}
