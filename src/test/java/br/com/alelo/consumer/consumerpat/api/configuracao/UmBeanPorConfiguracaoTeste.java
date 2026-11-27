package br.com.alelo.consumer.consumerpat.api.configuracao;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Impõe a regra de uma responsabilidade por arquivo na composition root: cada classe de
 * configuração declara, no máximo, um {@code @Bean}.
 *
 * <p>
 * Uma configuração com dois beans tem duas razões de mudar, e é o primeiro passo para ela virar um
 * lugar onde se põe tudo. Os filtros da requisição já foram assim, quatro num arquivo só, e a ordem
 * entre eles, que é contrato, ficava escondida entre os registros.
 */
class UmBeanPorConfiguracaoTeste {

    private static final Path PACOTE = Path
            .of("src/main/java/br/com/alelo/consumer/consumerpat/api/configuracao");

    private static List<Path> configuracoes() throws IOException {
        try (Stream<Path> arquivos = Files.list(PACOTE)) {
            return arquivos.filter(arquivo -> arquivo.toString().endsWith(".java")).toList();
        }
    }

    private static long beansDe(Path fonte) {
        try {
            return Files.readAllLines(fonte).stream().map(String::strip)
                    .filter(linha -> linha.equals("@Bean")).count();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void deve_encontrar_as_configuracoes_quando_listar_o_pacote() throws IOException {
        // Agir
        var fontes = configuracoes();

        // Verificar: sem isto, o teste passaria vazio se o pacote mudasse de lugar
        assertThat(fontes).hasSizeGreaterThanOrEqualTo(10);
    }

    @Test
    void deve_declarar_no_maximo_um_bean_quando_contar_por_configuracao() throws IOException {
        // Agir
        var fontes = configuracoes();

        // Verificar
        assertThat(fontes).allSatisfy(fonte -> assertThat(beansDe(fonte))
                .as("beans em " + fonte.getFileName()).isLessThanOrEqualTo(1));
    }
}
