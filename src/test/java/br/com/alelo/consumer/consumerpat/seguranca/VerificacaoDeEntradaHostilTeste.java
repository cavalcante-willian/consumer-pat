package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VerificacaoDeEntradaHostilTeste {

    private final VerificacaoDeEntradaHostil verificacao = new VerificacaoDeEntradaHostil();

    @Test
    void deve_proteger_quando_toda_carga_e_recusada_e_a_listagem_segue_de_pe() throws Exception {
        // Agir
        var achado = verificacao.verificar(new SondaFalsa(true));

        // Verificar
        assertThat(achado.chave()).isEqualTo("entrada-hostil-nao-vira-sql");
        assertThat(achado.protegido()).isTrue();
        assertThat(achado.evidencia()).contains("15 tentativas").contains("0 falhas de servidor")
                .contains("listagem depois: 200");
    }

    @Test
    void deve_mandar_cada_carga_ao_nome_ao_cartao_e_a_ordenacao_quando_verificar()
            throws Exception {
        // Preparar
        var sonda = new SondaFalsa(true);

        // Agir
        verificacao.verificar(sonda);

        // Verificar: as cinco cargas vão aos três pontos, e a listagem sadia fecha a verificação
        assertThat(sonda.nomesRecebidos).hasSize(5).anyMatch(carga -> carga.contains("DROP TABLE"))
                .anyMatch(carga -> carga.contains("SHUTDOWN"))
                .anyMatch(carga -> carga.contains("CREATE ALIAS"))
                .anyMatch(carga -> carga.contains("UNION"));
        assertThat(sonda.numerosRecebidos).isEqualTo(sonda.nomesRecebidos);
        assertThat(sonda.ordenacoesRecebidas).startsWith(sonda.nomesRecebidos.get(0))
                .endsWith("nome").hasSize(6);
    }

    @Test
    void deve_expor_quando_uma_carga_derruba_o_servidor_com_500() throws Exception {
        // Preparar
        var sonda = new SondaFalsa(true);
        sonda.recusa = SondaFalsa.resposta(500, "{}");

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar
        assertThat(achado.protegido()).isFalse();
        assertThat(achado.evidencia()).contains("15 falhas de servidor");
    }

    @Test
    void deve_expor_quando_a_resposta_traz_texto_de_erro_de_banco() throws Exception {
        // Preparar: recusa com 400, mas contando como o SQL quebrou
        var sonda = new SondaFalsa(true);
        sonda.recusa = SondaFalsa.resposta(400,
                "{\"detail\":\"org.hibernate.exception.SQLGrammarException: syntax error\"}");

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar
        assertThat(achado.protegido()).isFalse();
        assertThat(achado.evidencia()).contains("org.hibernate").contains("syntax error");
    }
}
