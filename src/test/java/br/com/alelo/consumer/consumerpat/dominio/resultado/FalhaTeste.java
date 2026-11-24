package br.com.alelo.consumer.consumerpat.dominio.resultado;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

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
}
