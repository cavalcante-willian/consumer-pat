package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VerificacaoDeErroDeClienteSem500Teste {

    private final Verificacao verificacao = new VerificacaoDeErroDeClienteSem500();

    @Test
    void deve_proteger_quando_o_cartao_inexistente_responde_404() throws Exception {
        // Agir
        var achado = verificacao.verificar(new SondaFalsa(true));

        // Verificar
        assertThat(achado.chave()).isEqualTo("erro-de-cliente-nao-vira-500");
        assertThat(achado.protegido()).isTrue();
        assertThat(achado.evidencia()).contains("status 404");
    }

    @Test
    void deve_expor_quando_o_cartao_inexistente_vira_falha_de_servidor() throws Exception {
        // Preparar: o legado lança NullPointerException e responde 500
        var sonda = new SondaFalsa(true);
        sonda.naoEncontrado = SondaFalsa.resposta(500, "{}");

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar
        assertThat(achado.protegido()).isFalse();
        assertThat(achado.evidencia()).contains("status 500");
    }
}
