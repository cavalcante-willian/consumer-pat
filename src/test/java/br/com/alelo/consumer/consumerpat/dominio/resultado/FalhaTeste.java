package br.com.alelo.consumer.consumerpat.dominio.resultado;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FalhaTeste {

    @Test
    void deve_usar_a_funcao_da_falha_quando_resolver() {
        // Preparar
        Resultado<Integer, String> resultado = new Falha<>("quebrou");

        // Agir
        var texto = resultado.resolver(valor -> "valor " + valor, erro -> "erro " + erro);

        // Verificar
        assertThat(texto).isEqualTo("erro quebrou");
    }

    @Test
    void deve_manter_o_erro_quando_mapear() {
        // Preparar
        Resultado<Integer, String> resultado = new Falha<>("quebrou");

        // Agir
        var mapeado = resultado.mapear(valor -> valor * 10);

        // Verificar
        assertThat(mapeado).isEqualTo(new Falha<Integer, String>("quebrou"));
    }

    @Test
    void deve_nao_executar_a_funcao_quando_mapear() {
        // Preparar
        Resultado<Integer, String> resultado = new Falha<>("quebrou");
        var chamadas = new AtomicInteger();

        // Agir
        resultado.mapear(valor -> chamadas.incrementAndGet());

        // Verificar
        assertThat(chamadas.get()).isZero();
    }

    @Test
    void deve_manter_o_erro_quando_encadear() {
        // Preparar
        Resultado<Integer, String> resultado = new Falha<>("quebrou");

        // Agir
        var encadeado = resultado.<Integer>encadear(valor -> new Sucesso<>(valor + 1));

        // Verificar
        assertThat(encadeado).isEqualTo(new Falha<Integer, String>("quebrou"));
    }

    @Test
    void deve_nao_executar_a_operacao_seguinte_quando_encadear() {
        // Preparar
        Resultado<Integer, String> resultado = new Falha<>("quebrou");
        var chamadas = new AtomicInteger();

        // Agir
        resultado.<Integer>encadear(valor -> new Sucesso<>(chamadas.incrementAndGet()));

        // Verificar
        assertThat(chamadas.get()).isZero();
    }

    @Test
    void deve_executar_a_acao_com_o_erro_e_devolver_a_mesma_falha_quando_observar_a_falha() {
        // Preparar
        Resultado<Integer, String> resultado = new Falha<>("quebrou");
        var vistos = new ArrayList<String>();

        // Agir
        var observado = resultado.aoFalhar(vistos::add);

        // Verificar: o efeito aconteceu uma vez, e a falha segue sendo um valor devolvido
        assertThat(vistos).containsExactly("quebrou");
        assertThat(observado).isEqualTo(resultado);
    }

    @Test
    void deve_nao_executar_a_acao_e_devolver_a_mesma_falha_quando_observar_o_sucesso() {
        // Preparar
        Resultado<Integer, String> resultado = new Falha<>("quebrou");
        var chamadas = new AtomicInteger();

        // Agir
        var observado = resultado.aoSucesso(valor -> chamadas.incrementAndGet());

        // Verificar
        assertThat(chamadas.get()).isZero();
        assertThat(observado).isEqualTo(resultado);
    }

    @Test
    void deve_propagar_a_excecao_da_acao_sem_engoli_la_quando_observar_a_falha() {
        // Preparar
        Resultado<Integer, String> resultado = new Falha<>("quebrou");

        // Agir e Verificar
        assertThatThrownBy(() -> resultado.aoFalhar(erro -> {
            throw new IllegalStateException("falhou ao observar");
        })).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deve_permitir_observar_o_sucesso_e_a_falha_na_mesma_cadeia() {
        // Preparar
        Resultado<Integer, String> resultado = new Falha<>("quebrou");
        var eventos = new ArrayList<String>();

        // Agir
        var observado = resultado.aoSucesso(valor -> eventos.add("sucesso " + valor))
                .aoFalhar(erro -> eventos.add("falha " + erro));

        // Verificar: só o lado que aconteceu é observado, e a falha chega intacta a quem chamou
        assertThat(eventos).containsExactly("falha quebrou");
        assertThat(observado).isEqualTo(resultado);
    }
}
