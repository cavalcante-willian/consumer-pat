package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VerificacaoDeValorEnormeTeste {

    private final VerificacaoDeValorEnorme verificacao = new VerificacaoDeValorEnorme();

    @Test
    void deve_proteger_quando_o_sistema_recusa_o_valor_enorme_e_o_saldo_fica() throws Exception {
        // Agir
        var achado = verificacao.verificar(new SondaFalsa(true));

        // Verificar
        assertThat(achado.chave()).isEqualTo("valor-enorme");
        assertThat(achado.protegido()).isTrue();
        assertThat(achado.evidencia()).contains("status 400").contains("saldo intacto");
    }

    @Test
    void deve_expor_quando_o_sistema_aceita_o_valor_enorme_e_o_saldo_muda() throws Exception {
        // Agir
        var achado = verificacao.verificar(new SondaFalsa(false));

        // Verificar
        assertThat(achado.protegido()).isFalse();
        assertThat(achado.evidencia()).contains("status 201").contains("saldo alterado");
    }
}
