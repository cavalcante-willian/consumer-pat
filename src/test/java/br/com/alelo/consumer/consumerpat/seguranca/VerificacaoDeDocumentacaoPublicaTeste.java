package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VerificacaoDeDocumentacaoPublicaTeste {

    private final Verificacao verificacao = new VerificacaoDeDocumentacaoPublica();

    @Test
    void deve_proteger_quando_o_swagger_e_a_especificacao_estao_fechados() throws Exception {
        // Agir
        var achado = verificacao.verificar(new SondaFalsa(true));

        // Verificar
        assertThat(achado.chave()).isEqualTo("documentacao-publica");
        assertThat(achado.protegido()).isTrue();
    }

    @Test
    void deve_expor_quando_os_dois_estao_abertos() throws Exception {
        // Preparar
        var sonda = new SondaFalsa(true);
        sonda.swagger = SondaFalsa.resposta(200, "<html>");
        sonda.especificacao = SondaFalsa.resposta(200, "{\"openapi\":\"3.0.1\"}");

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar
        assertThat(achado.protegido()).isFalse();
        assertThat(achado.evidencia()).contains("Swagger: status 200")
                .contains("especificação: status 200");
    }

    @Test
    void deve_expor_quando_so_a_especificacao_esta_aberta() throws Exception {
        // Preparar: fechar a página e esquecer o JSON deixa o contrato público
        var sonda = new SondaFalsa(true);
        sonda.especificacao = SondaFalsa.resposta(200, "{}");

        // Agir e Verificar
        assertThat(verificacao.verificar(sonda).protegido()).isFalse();
    }

    @Test
    void deve_expor_quando_so_o_swagger_esta_aberto() throws Exception {
        // Preparar
        var sonda = new SondaFalsa(true);
        sonda.swagger = SondaFalsa.resposta(200, "<html>");

        // Agir e Verificar
        assertThat(verificacao.verificar(sonda).protegido()).isFalse();
    }
}
