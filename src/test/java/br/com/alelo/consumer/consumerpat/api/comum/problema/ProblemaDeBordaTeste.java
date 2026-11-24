package br.com.alelo.consumer.consumerpat.api.comum.problema;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Garante o contrato dos problemas de borda como o teste de códigos garante o dos erros de domínio:
 * um problema novo com código repetido, fora do formato ou com status que não é de erro quebra o
 * build.
 */
class ProblemaDeBordaTeste {

    private static final String FORMATO = "CPT-[A-Z]{3}-[A-Z]{3}-\\d{3}";

    @ParameterizedTest
    @EnumSource(ProblemaDeBorda.class)
    void deve_ter_codigo_no_formato_quando_consultar_qualquer_problema(ProblemaDeBorda problema) {
        // Verificar
        assertThat(problema.codigo()).matches(FORMATO);
    }

    @ParameterizedTest
    @EnumSource(ProblemaDeBorda.class)
    void deve_ter_titulo_e_status_de_erro_quando_consultar_qualquer_problema(
            ProblemaDeBorda problema) {
        // Verificar
        assertThat(problema.titulo()).isNotBlank();
        assertThat(problema.statusHttp()).isBetween(400, 599);
    }

    @Test
    void deve_ter_codigo_unico_quando_percorrer_todos_os_problemas() {
        // Agir
        var codigos = Arrays.stream(ProblemaDeBorda.values()).map(ProblemaDeBorda::codigo).toList();

        // Verificar
        assertThat(Set.copyOf(codigos)).hasSameSizeAs(codigos);
    }
}
