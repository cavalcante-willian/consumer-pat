package br.com.alelo.consumer.consumerpat.dominio.resultado;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SucessoTeste {

    @Test
    void deve_usar_a_funcao_do_sucesso_quando_resolver() {
        // Preparar
        Resultado<Integer, String> resultado = new Sucesso<>(2);

        // Agir
        var texto = resultado.resolver(valor -> "valor " + valor, erro -> "erro " + erro);

        // Verificar
        assertThat(texto).isEqualTo("valor 2");
    }

    @Test
    void deve_transformar_o_valor_quando_mapear() {
        // Preparar
        Resultado<Integer, String> resultado = new Sucesso<>(2);

        // Agir
        var mapeado = resultado.mapear(valor -> valor * 10);

        // Verificar
        assertThat(mapeado).isEqualTo(new Sucesso<Integer, String>(20));
    }

    @Test
    void deve_devolver_o_resultado_da_operacao_seguinte_quando_encadear_um_sucesso() {
        // Preparar
        Resultado<Integer, String> resultado = new Sucesso<>(2);

        // Agir
        var encadeado = resultado.<Integer>encadear(valor -> new Sucesso<>(valor + 1));

        // Verificar
        assertThat(encadeado).isEqualTo(new Sucesso<Integer, String>(3));
    }

    @Test
    void deve_devolver_a_falha_da_operacao_seguinte_quando_encadear_e_ela_falha() {
        // Preparar
        Resultado<Integer, String> resultado = new Sucesso<>(2);

        // Agir
        var encadeado = resultado.<Integer>encadear(valor -> new Falha<>("não pode"));

        // Verificar
        assertThat(encadeado).isEqualTo(new Falha<Integer, String>("não pode"));
    }
}
