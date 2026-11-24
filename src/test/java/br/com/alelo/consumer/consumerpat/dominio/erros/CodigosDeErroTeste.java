package br.com.alelo.consumer.consumerpat.dominio.erros;

import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroValidacao;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Garante o contrato dos códigos de erro percorrendo a hierarquia: variante
 * nova sem código, com
 * código repetido ou fora do formato quebra o build. Categoria nova entra na
 * lista de categorias.
 */
class CodigosDeErroTeste {

    private static final List<Class<?>> CATEGORIAS = List.of(ErroValidacao.class);

    private static final Pattern FORMATO = Pattern.compile("CPT-[A-Z]{3}-[A-Z]{3}-\\d{3}");

    private static List<Class<?>> variantes() {
        return CATEGORIAS.stream()
                .flatMap(categoria -> Arrays.stream(categoria.getPermittedSubclasses()))
                .toList();
    }

    private static String codigoDe(Class<?> variante) {
        try {
            return (String) variante.getField("CODIGO").get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("A variante " + variante.getSimpleName()
                    + " precisa declarar a constante pública estática CODIGO", e);
        }
    }

    @Test
    void deve_ter_codigo_no_formato_quando_percorrer_todas_as_variantes() {
        // Agir
        var codigos = variantes().stream().map(CodigosDeErroTeste::codigoDe).toList();

        // Verificar
        assertThat(codigos).isNotEmpty().allMatch(codigo -> FORMATO.matcher(codigo).matches());
    }

    @Test
    void deve_ter_codigo_unico_quando_percorrer_todas_as_variantes() {
        // Agir
        var codigos = variantes().stream().map(CodigosDeErroTeste::codigoDe).toList();

        // Verificar
        assertThat(Set.copyOf(codigos)).hasSameSizeAs(codigos);
    }

    @Test
    void deve_declarar_o_codigo_da_variante_na_constante_quando_consultar_o_erro() {
        // Agir
        var codigosDasInstancias = List.of(
                new br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroValorMonetarioInvalido().codigo());
        var codigosDasConstantes = variantes().stream().map(CodigosDeErroTeste::codigoDe).toList();

        // Verificar
        assertThat(codigosDasInstancias).containsExactlyElementsOf(codigosDasConstantes);
    }
}
