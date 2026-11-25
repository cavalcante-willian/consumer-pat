package br.com.alelo.consumer.consumerpat.dominio.resultado;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ResultadoTeste {

    @Test
    void deve_ter_so_sucesso_e_falha_como_variantes_quando_listar_as_permitidas() {
        // Agir
        var variantes = Resultado.class.getPermittedSubclasses();

        // Verificar
        assertThat(variantes).containsExactlyInAnyOrder(Sucesso.class, Falha.class);
    }

    @Test
    void deve_ser_os_cinco_papeis_quando_consultar_os_tipos_que_ele_estende() {
        // Verificar
        assertThat(Resolvivel.class).isAssignableFrom(Resultado.class);
        assertThat(Transformavel.class).isAssignableFrom(Resultado.class);
        assertThat(Encadeavel.class).isAssignableFrom(Resultado.class);
        assertThat(ObservavelNoSucesso.class).isAssignableFrom(Resultado.class);
        assertThat(ObservavelNaFalha.class).isAssignableFrom(Resultado.class);
    }

    @Test
    void deve_ter_o_resolver_como_unica_operacao_abstrata_quando_consultar_os_papeis() {
        // Agir: as variantes só implementam o resolver, e o resto é escrito em cima dele
        var abstratas = Arrays.stream(Resultado.class.getMethods())
                .filter(metodo -> Modifier.isAbstract(metodo.getModifiers())).map(Method::getName)
                .toList();

        // Verificar
        assertThat(abstratas).containsExactly("resolver");
    }
}
