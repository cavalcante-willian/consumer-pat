package br.com.alelo.consumer.consumerpat.dominio.erros;

import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroConflito;
import br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado.ErroNaoEncontrado;
import br.com.alelo.consumer.consumerpat.dominio.erros.regradenegocio.ErroRegraDeNegocio;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroValidacao;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Garante o contrato dos códigos de erro percorrendo a hierarquia: variante nova sem código, com
 * código repetido ou fora do formato quebra o build. Categoria nova entra na lista de categorias.
 */
class CodigosDeErroTeste {

    private static final List<Class<?>> CATEGORIAS = List.of(ErroConflito.class,
            ErroNaoEncontrado.class, ErroRegraDeNegocio.class, ErroValidacao.class);

    private static final Pattern FORMATO = Pattern.compile("CPT-[A-Z]{3}-[A-Z]{3}-\\d{3}");

    private static List<Class<?>> variantes() {
        return CATEGORIAS.stream()
                .flatMap(categoria -> Arrays.stream(categoria.getPermittedSubclasses())).toList();
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
        var codigosDasInstancias = variantes().stream().map(CodigosDeErroTeste::instanciar)
                .map(ErroDominio::codigo).toList();
        var codigosDasConstantes = variantes().stream().map(CodigosDeErroTeste::codigoDe).toList();

        // Verificar
        assertThat(codigosDasInstancias).containsExactlyElementsOf(codigosDasConstantes);
    }

    /**
     * Cria a variante com o valor padrão em cada componente (zero ou nulo): o código não depende
     * dos dados do erro, então o teste não precisa saber como cada variante se constrói, e uma
     * variante nova entra na verificação sem editar esta lista.
     */
    private static ErroDominio instanciar(Class<?> variante) {
        try {
            var construtor = variante.getDeclaredConstructors()[0];
            var padroes = Arrays.stream(construtor.getParameterTypes())
                    .map(tipo -> Array.get(Array.newInstance(tipo, 1), 0)).toArray();
            return (ErroDominio) construtor.newInstance(padroes);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Não foi possível criar a variante " + variante.getSimpleName(), e);
        }
    }
}
