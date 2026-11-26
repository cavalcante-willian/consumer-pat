package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VerificacaoDeCorpoGiganteTeste {

    private final VerificacaoDeCorpoGigante verificacao = new VerificacaoDeCorpoGigante();

    @Test
    void deve_proteger_quando_o_corpo_gigante_e_recusado_pelo_cliente() throws Exception {
        // Preparar
        var sonda = new SondaFalsa(true);

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar: o nome mandado tem seis milhões de caracteres
        assertThat(achado.chave()).isEqualTo("corpo-gigante");
        assertThat(achado.protegido()).isTrue();
        assertThat(sonda.nomesRecebidos).singleElement()
                .satisfies(nome -> assertThat(nome).hasSize(6_000_000));
    }

    @Test
    void deve_expor_quando_o_corpo_gigante_derruba_o_servidor() throws Exception {
        // Preparar
        var sonda = new SondaFalsa(true);
        sonda.recusa = SondaFalsa.resposta(500, "");

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar
        assertThat(achado.protegido()).isFalse();
        assertThat(achado.evidencia()).contains("status 500");
    }

    @Test
    void deve_expor_quando_o_corpo_gigante_e_aceito() throws Exception {
        // Preparar
        var sonda = new SondaFalsa(true);
        sonda.recusa = SondaFalsa.resposta(201, "");

        // Agir e Verificar
        assertThat(verificacao.verificar(sonda).protegido()).isFalse();
    }
}
