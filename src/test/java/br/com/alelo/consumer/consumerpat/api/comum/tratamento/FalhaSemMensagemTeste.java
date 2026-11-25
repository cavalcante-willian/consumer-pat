package br.com.alelo.consumer.consumerpat.api.comum.tratamento;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FalhaSemMensagemTeste {

    @Test
    void deve_guardar_so_o_tipo_quando_a_mensagem_tem_dado_sensivel() {
        // Preparar
        var original = new IllegalStateException("cartao 5555000011112222 senha=segredo");

        // Agir
        var falha = new FalhaSemMensagem(original);

        // Verificar
        assertThat(falha.getMessage()).isEqualTo("java.lang.IllegalStateException");
        assertThat(falha.toString()).doesNotContain("5555000011112222").doesNotContain("segredo");
    }

    @Test
    void deve_guardar_a_pilha_inteira_quando_o_operador_precisa_achar_o_defeito() {
        // Preparar
        var original = new IllegalStateException("x");

        // Agir
        var falha = new FalhaSemMensagem(original);

        // Verificar
        assertThat(falha.getStackTrace()).isEqualTo(original.getStackTrace());
        assertThat(falha.getStackTrace()).isNotEmpty();
    }

    @Test
    void deve_guardar_a_cadeia_de_causas_sem_as_mensagens_quando_ha_causa_raiz() {
        // Preparar
        var raiz = new IllegalArgumentException("valor 12345678901");
        var original = new IllegalStateException("meio 4111111111111111", raiz);

        // Agir
        var falha = new FalhaSemMensagem(original);

        // Verificar: a causa-raiz também perde a mensagem e mantém a pilha
        assertThat(falha.getCause()).isInstanceOf(FalhaSemMensagem.class);
        assertThat(falha.getCause().getMessage()).isEqualTo("java.lang.IllegalArgumentException");
        assertThat(falha.getCause().getStackTrace()).isEqualTo(raiz.getStackTrace());
        assertThat(falha.getCause().getCause()).isNull();
    }

    @Test
    void deve_nao_ter_causa_quando_a_original_nao_tem() {
        // Agir
        var falha = new FalhaSemMensagem(new IllegalStateException("x"));

        // Verificar
        assertThat(falha.getCause()).isNull();
    }
}
