package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AchadoTeste {

    @Test
    void deve_ser_protegido_quando_a_verificacao_diz_que_o_sistema_se_defendeu() {
        // Agir
        var achado = new Achado("chave", "descrição", true, "status 400");

        // Verificar
        assertThat(achado.veredito()).isEqualTo(Veredito.PROTEGIDO);
        assertThat(achado.protegido()).isTrue();
    }

    @Test
    void deve_ser_exposto_quando_a_verificacao_diz_que_o_sistema_deixou_passar() {
        // Agir
        var achado = new Achado("chave", "descrição", false, "status 200");

        // Verificar
        assertThat(achado.veredito()).isEqualTo(Veredito.EXPOSTO);
        assertThat(achado.protegido()).isFalse();
    }

    @Test
    void deve_nao_ser_protegido_nem_exposto_quando_a_verificacao_nao_pode_ser_feita() {
        // Agir
        var achado = Achado.naoVerificado("chave", "descrição", "sem interface de rede");

        // Verificar: não verificar não é se proteger, e a evidência é o motivo
        assertThat(achado.veredito()).isEqualTo(Veredito.NAO_VERIFICADO);
        assertThat(achado.protegido()).isFalse();
        assertThat(achado.evidencia()).isEqualTo("sem interface de rede");
    }

    @Test
    void deve_guardar_a_chave_a_descricao_e_a_evidencia_quando_construir() {
        // Agir
        var achado = new Achado("chave", "descrição", true, "evidência");

        // Verificar
        assertThat(achado.chave()).isEqualTo("chave");
        assertThat(achado.descricao()).isEqualTo("descrição");
        assertThat(achado.evidencia()).isEqualTo("evidência");
    }

    @Test
    void deve_ter_o_rotulo_de_cada_veredito_do_mesmo_tamanho_nos_dois_primeiros() {
        // Agir e Verificar: o relatório alinha o traço depois dos dois que mais aparecem
        assertThat(Veredito.PROTEGIDO.rotulo()).hasSameSizeAs(Veredito.EXPOSTO.rotulo());
        assertThat(Veredito.NAO_VERIFICADO.rotulo()).isEqualTo("NÃO VERIFICADO");
    }
}
