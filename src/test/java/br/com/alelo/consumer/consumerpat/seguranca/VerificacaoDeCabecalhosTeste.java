package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VerificacaoDeCabecalhosTeste {

    private final VerificacaoDeCabecalhos verificacao = new VerificacaoDeCabecalhos();

    @Test
    void deve_proteger_quando_os_cinco_cabecalhos_estao_presentes() throws Exception {
        // Agir
        var achado = verificacao.verificar(new SondaFalsa(true));

        // Verificar
        assertThat(achado.chave()).isEqualTo("cabecalhos-de-seguranca");
        assertThat(achado.protegido()).isTrue();
        assertThat(achado.evidencia()).contains("faltam: []");
    }

    @Test
    void deve_expor_e_dizer_todos_quando_a_resposta_nao_traz_nenhum() throws Exception {
        // Preparar
        var sonda = new SondaFalsa(true);
        sonda.valida = SondaFalsa.resposta(200, "[]");

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar
        assertThat(achado.protegido()).isFalse();
        assertThat(achado.evidencia()).contains("X-Content-Type-Options", "X-Frame-Options",
                "Content-Security-Policy", "Referrer-Policy", "Cache-Control");
    }

    @Test
    void deve_expor_e_dizer_so_o_que_falta_quando_falta_um() throws Exception {
        // Preparar: tudo, menos a política de conteúdo
        var sonda = new SondaFalsa(true);
        sonda.valida = SondaFalsa.resposta(200, "{}",
                Map.of("x-content-type-options", "nosniff", "x-frame-options", "DENY",
                        "referrer-policy", "no-referrer", "cache-control", "no-store"));

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar
        assertThat(achado.protegido()).isFalse();
        assertThat(achado.evidencia()).contains("faltam: [Content-Security-Policy]");
    }
}
