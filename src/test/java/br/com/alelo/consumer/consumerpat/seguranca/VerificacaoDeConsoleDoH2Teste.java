package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class VerificacaoDeConsoleDoH2Teste {

    private final Verificacao verificacao = new VerificacaoDeConsoleDoH2();

    @Test
    void deve_proteger_quando_de_fora_o_h2_responde_a_pagina_de_recusa_com_status_200()
            throws Exception {
        // Preparar: o H2 recusa quem vem de fora com uma página, e não com um erro HTTP; pela
        // própria máquina o console abre
        var sonda = new SondaFalsa(true);
        sonda.console = SondaFalsa.resposta(200, "<form>login do H2</form>");

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar: o status 200 sozinho enganava, e o corpo é o que diz que foi recusado
        assertThat(achado.chave()).isEqualTo("console-do-h2");
        assertThat(achado.veredito()).isEqualTo(Veredito.PROTEGIDO);
        assertThat(achado.evidencia()).contains("de fora (pela interface de rede): status 200")
                .contains("página de recusa do H2: true")
                .contains("pela própria máquina: status 200, aberto por desenho");
    }

    @Test
    void deve_proteger_quando_de_fora_o_sistema_responde_um_erro_de_cliente() throws Exception {
        // Preparar: uma proteção que recusa de verdade, com 403
        var sonda = new SondaFalsa(true);
        sonda.consoleDeFora = Optional.of(SondaFalsa.resposta(403, ""));

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar
        assertThat(achado.veredito()).isEqualTo(Veredito.PROTEGIDO);
        assertThat(achado.evidencia()).contains("status 403")
                .contains("página de recusa do H2: false");
    }

    @Test
    void deve_expor_quando_de_fora_o_console_abre_o_formulario_de_login() throws Exception {
        // Preparar: o H2 com webAllowOthers ligado, ou um proxy que o faz parecer local
        var sonda = new SondaFalsa(true);
        sonda.consoleDeFora = Optional.of(SondaFalsa.resposta(200, "<form>login do H2</form>"));

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar
        assertThat(achado.veredito()).isEqualTo(Veredito.EXPOSTO);
        assertThat(achado.evidencia()).contains("status 200")
                .contains("página de recusa do H2: false");
    }

    @Test
    void deve_dizer_nao_verificado_quando_a_maquina_nao_tem_interface_de_rede() throws Exception {
        // Preparar: sem endereço fora do loopback, não há pedido de fora a fazer
        var sonda = new SondaFalsa(true);
        sonda.consoleDeFora = Optional.empty();
        sonda.console = SondaFalsa.resposta(200, "<form>login do H2</form>");

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar: o pedido local aberto não vira "exposto", que era o erro da primeira versão
        assertThat(achado.veredito()).isEqualTo(Veredito.NAO_VERIFICADO);
        assertThat(achado.protegido()).isFalse();
        assertThat(achado.evidencia()).contains("não tem interface de rede")
                .contains("pela própria máquina: status 200");
    }

    @Test
    void deve_proteger_mesmo_quando_a_propria_maquina_abre_o_console() throws Exception {
        // Preparar: o console abre localmente por desenho do README, e de fora é recusado
        var sonda = new SondaFalsa(true);
        sonda.console = SondaFalsa.resposta(200, "<form>login do H2</form>");

        // Agir e Verificar: o que se protege é quem vem de fora, e o local aberto não derruba
        assertThat(verificacao.verificar(sonda).protegido()).isTrue();
    }
}
