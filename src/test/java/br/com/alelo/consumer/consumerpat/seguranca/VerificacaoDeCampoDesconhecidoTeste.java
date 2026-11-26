package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VerificacaoDeCampoDesconhecidoTeste {

    private final VerificacaoDeCampoDesconhecido verificacao = new VerificacaoDeCampoDesconhecido();

    @Test
    void deve_proteger_quando_o_campo_desconhecido_e_recusado_com_400() throws Exception {
        // Agir
        var achado = verificacao.verificar(new SondaFalsa(true));

        // Verificar
        assertThat(achado.chave()).isEqualTo("campo-desconhecido");
        assertThat(achado.protegido()).isTrue();
    }

    @Test
    void deve_expor_quando_o_cadastro_com_campo_desconhecido_e_aceito() throws Exception {
        // Preparar
        var sonda = new SondaFalsa(true);
        sonda.recusa = SondaFalsa.resposta(201, "{}");

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar
        assertThat(achado.protegido()).isFalse();
        assertThat(achado.evidencia()).contains("status 201");
    }

    @Test
    void deve_expor_quando_o_sistema_recusa_com_outro_status_que_nao_e_o_400() throws Exception {
        // Preparar: o legado grava e responde 404, e isso não é recusar
        var sonda = new SondaFalsa(true);
        sonda.recusa = SondaFalsa.resposta(404, "");

        // Agir e Verificar
        assertThat(verificacao.verificar(sonda).protegido()).isFalse();
    }
}
