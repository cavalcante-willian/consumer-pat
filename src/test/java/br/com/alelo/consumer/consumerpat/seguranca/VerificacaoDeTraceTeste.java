package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VerificacaoDeTraceTeste {

    private final Verificacao verificacao = new VerificacaoDeTrace();

    @Test
    void deve_proteger_quando_o_trace_e_recusado_sem_eco() throws Exception {
        // Agir
        var achado = verificacao.verificar(new SondaFalsa(true));

        // Verificar
        assertThat(achado.chave()).isEqualTo("trace-sem-eco");
        assertThat(achado.protegido()).isTrue();
        assertThat(achado.evidencia()).contains("status 405")
                .contains("eco dos cabeçalhos da " + "requisição: false");
    }

    @Test
    void deve_expor_quando_o_corpo_devolve_os_cabecalhos_da_requisicao() throws Exception {
        // Preparar: o eco do HttpServlet
        var sonda = new SondaFalsa(true);
        sonda.trace = SondaFalsa.resposta(405,
                "TRACE /error HTTP/1.1\r\nHost: localhost:8080\r\ncookie: sessao=segredo\r\n");

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar
        assertThat(achado.protegido()).isFalse();
        assertThat(achado.evidencia()).contains("eco dos cabeçalhos da requisição: true");
    }

    @Test
    void deve_reconhecer_o_eco_sem_distinguir_maiusculas_quando_o_cabecalho_vem_em_outra_caixa()
            throws Exception {
        // Preparar
        var sonda = new SondaFalsa(true);
        sonda.trace = SondaFalsa.resposta(200, "HOST: localhost");

        // Agir e Verificar
        assertThat(verificacao.verificar(sonda).protegido()).isFalse();
    }
}
