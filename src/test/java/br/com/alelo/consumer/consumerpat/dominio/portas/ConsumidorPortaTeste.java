package br.com.alelo.consumer.consumerpat.dominio.portas;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ConsumidorPortaTeste {

    @Test
    void deve_ser_a_busca_a_criacao_e_a_atualizacao_quando_consultar_os_papeis() {
        // Verificar
        assertThat(BuscadorDeConsumidor.class).isAssignableFrom(ConsumidorPorta.class);
        assertThat(ListadorDeConsumidores.class).isAssignableFrom(ConsumidorPorta.class);
        assertThat(GravadorDeNovoConsumidor.class).isAssignableFrom(ConsumidorPorta.class);
        assertThat(AtualizadorDeConsumidor.class).isAssignableFrom(ConsumidorPorta.class);
    }

    @Test
    void deve_oferecer_so_as_tres_operacoes_quando_listar_as_operacoes_da_porta() {
        // Agir
        var operacoes = Arrays.stream(ConsumidorPorta.class.getMethods()).map(Method::getName)
                .toList();

        // Verificar
        assertThat(operacoes).containsExactlyInAnyOrder("buscarPorId", "listar", "criar",
                "atualizar");
    }

    @Test
    void deve_nao_declarar_operacao_propria_quando_consultar_os_metodos_declarados() {
        // Verificar
        assertThat(ConsumidorPorta.class.getDeclaredMethods()).isEmpty();
    }

    @Test
    void deve_ter_uma_operacao_por_papel_quando_consultar_cada_papel() {
        // Verificar: cada papel é um arquivo e uma operação
        assertThat(BuscadorDeConsumidor.class.getDeclaredMethods()).hasSize(1);
        assertThat(GravadorDeNovoConsumidor.class.getDeclaredMethods()).hasSize(1);
        assertThat(AtualizadorDeConsumidor.class.getDeclaredMethods()).hasSize(1);
    }
}
