package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VerificacaoDeConsoleDoH2Teste {

    private final Verificacao verificacao = new VerificacaoDeConsoleDoH2();

    @Test
    void deve_proteger_quando_o_console_esta_fechado() throws Exception {
        // Agir
        var achado = verificacao.verificar(new SondaFalsa(true));

        // Verificar
        assertThat(achado.chave()).isEqualTo("console-do-h2");
        assertThat(achado.protegido()).isTrue();
        assertThat(achado.evidencia()).isEqualTo("status 404");
    }

    @Test
    void deve_expor_quando_o_console_redireciona_para_a_tela_de_login() throws Exception {
        // Preparar: no perfil padrão, a rota responde 302
        var sonda = new SondaFalsa(true);
        sonda.console = SondaFalsa.resposta(302, "");

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar
        assertThat(achado.protegido()).isFalse();
        assertThat(achado.evidencia()).isEqualTo("status 302");
    }

    @Test
    void deve_proteger_quando_o_console_exige_autenticacao() throws Exception {
        // Preparar
        var sonda = new SondaFalsa(true);
        sonda.console = SondaFalsa.resposta(401, "");

        // Agir e Verificar
        assertThat(verificacao.verificar(sonda).protegido()).isTrue();
    }
}
