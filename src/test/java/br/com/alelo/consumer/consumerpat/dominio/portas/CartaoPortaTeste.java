package br.com.alelo.consumer.consumerpat.dominio.portas;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class CartaoPortaTeste {

    @Test
    void deve_ser_a_busca_para_atualizacao_e_a_gravacao_quando_consultar_os_papeis() {
        // Verificar
        assertThat(BuscadorDeCartaoParaAtualizacao.class).isAssignableFrom(CartaoPorta.class);
        assertThat(GravadorDeCartao.class).isAssignableFrom(CartaoPorta.class);
    }

    @Test
    void deve_oferecer_so_as_duas_operacoes_quando_listar_as_operacoes_da_porta() {
        // Agir
        var operacoes = Arrays.stream(CartaoPorta.class.getMethods()).map(Method::getName).toList();

        // Verificar
        assertThat(operacoes).containsExactlyInAnyOrder("buscarPorNumeroETipoParaAtualizacao",
                "salvar");
    }

    @Test
    void deve_nao_declarar_operacao_propria_quando_consultar_os_metodos_declarados() {
        // Verificar
        assertThat(CartaoPorta.class.getDeclaredMethods()).isEmpty();
    }
}
