package br.com.alelo.consumer.consumerpat.dominio.erros.conflito;

import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.erros.CategoriaErro;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErroNumeroDeCartaoJaCadastradoTeste {

    private static final ErroNumeroDeCartaoJaCadastrado ERRO = new ErroNumeroDeCartaoJaCadastrado(
            new NumeroDeCartao("5555000011112222"));

    @Test
    void deve_ser_da_categoria_conflito_com_status_409() {
        // Verificar
        assertThat(ERRO.categoria()).isEqualTo(CategoriaErro.CONFLITO);
        assertThat(ERRO.categoria().statusHttp()).isEqualTo(409);
    }

    @Test
    void deve_expor_o_codigo_da_constante_quando_consultar_o_codigo() {
        // Verificar
        assertThat(ERRO.codigo()).isEqualTo(ErroNumeroDeCartaoJaCadastrado.CODIGO);
    }

    @Test
    void deve_apontar_o_campo_dos_numeros_quando_consultar_os_campos_com_erro() {
        // Verificar
        assertThat(ERRO.camposComErro()).containsOnlyKeys("numerosCartoes");
    }

    @Test
    void deve_nunca_mostrar_o_numero_inteiro_quando_consultar_a_mensagem() {
        // Verificar
        assertThat(ERRO.mensagem()).contains("****2222").doesNotContain("5555000011112222");
        assertThat(ERRO.toString()).doesNotContain("5555000011112222");
    }
}
