package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VerificacaoDeDadoSensivelTeste {

    private final VerificacaoDeDadoSensivel verificacao = new VerificacaoDeDadoSensivel();

    @Test
    void deve_proteger_quando_documento_e_cartao_aparecem_mascarados() throws Exception {
        // Agir
        var achado = verificacao.verificar(new SondaFalsa(true));

        // Verificar
        assertThat(achado.chave()).isEqualTo("dado-sensivel-na-resposta");
        assertThat(achado.protegido()).isTrue();
        assertThat(achado.evidencia()).contains("documento por inteiro: false")
                .contains("cartão por inteiro: false");
    }

    @Test
    void deve_expor_quando_o_documento_aparece_por_inteiro() throws Exception {
        // Preparar
        var sonda = new SondaFalsa(true);
        sonda.exposicao = new ExposicaoDeDados("{\"documentNumber\":39053344705}", "39053344705",
                "4111111111111111");

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar
        assertThat(achado.protegido()).isFalse();
        assertThat(achado.evidencia()).contains("documento por inteiro: true")
                .contains("cartão por inteiro: false");
    }

    @Test
    void deve_expor_quando_o_cartao_aparece_por_inteiro() throws Exception {
        // Preparar
        var sonda = new SondaFalsa(true);
        sonda.exposicao = new ExposicaoDeDados("{\"cartao\":\"4111111111111111\"}", "39053344705",
                "4111111111111111");

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar
        assertThat(achado.protegido()).isFalse();
        assertThat(achado.evidencia()).contains("cartão por inteiro: true");
    }
}
