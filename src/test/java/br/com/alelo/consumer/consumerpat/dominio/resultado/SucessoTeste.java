package br.com.alelo.consumer.consumerpat.dominio.resultado;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void deve_executar_a_acao_com_o_valor_e_devolver_o_mesmo_sucesso_quando_observar_o_sucesso() {
        // Preparar
        Resultado<Integer, String> resultado = new Sucesso<>(2);
        var vistos = new ArrayList<Integer>();

        // Agir
        var observado = resultado.aoSucesso(vistos::add);

        // Verificar: o efeito aconteceu uma vez, e o resultado não mudou
        assertThat(vistos).containsExactly(2);
        assertThat(observado).isEqualTo(resultado);
    }

    @Test
    void deve_nao_executar_a_acao_e_devolver_o_mesmo_sucesso_quando_observar_a_falha() {
        // Preparar
        Resultado<Integer, String> resultado = new Sucesso<>(2);
        var chamadas = new AtomicInteger();

        // Agir
        var observado = resultado.aoFalhar(erro -> chamadas.incrementAndGet());

        // Verificar
        assertThat(chamadas.get()).isZero();
        assertThat(observado).isEqualTo(resultado);
    }

    @Test
    void deve_propagar_a_excecao_da_acao_sem_engoli_la_quando_observar_o_sucesso() {
        // Preparar
        Resultado<Integer, String> resultado = new Sucesso<>(2);

        // Agir e Verificar: observar não esconde o defeito de quem observa
        assertThatThrownBy(() -> resultado.aoSucesso(valor -> {
            throw new IllegalStateException("falhou ao observar");
        })).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deve_permitir_observar_o_sucesso_e_a_falha_na_mesma_cadeia() {
        // Preparar
        Resultado<Integer, String> resultado = new Sucesso<>(2);
        var eventos = new ArrayList<String>();

        // Agir
        var observado = resultado.aoSucesso(valor -> eventos.add("sucesso " + valor))
                .aoFalhar(erro -> eventos.add("falha " + erro));

        // Verificar: só o lado que aconteceu é observado, e o resultado segue igual
        assertThat(eventos).containsExactly("sucesso 2");
        assertThat(observado).isEqualTo(resultado);
    }
}
