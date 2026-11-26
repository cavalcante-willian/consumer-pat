package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VerificacaoDeSaldoPorAtualizacaoTeste {

    private final Verificacao verificacao = new VerificacaoDeSaldoPorAtualizacao();

    @Test
    void deve_proteger_quando_a_atualizacao_com_saldo_e_recusada() throws Exception {
        // Agir
        var achado = verificacao.verificar(new SondaFalsa(true));

        // Verificar
        assertThat(achado.chave()).isEqualTo("saldo-por-atualizacao");
        assertThat(achado.protegido()).isTrue();
        assertThat(achado.evidencia()).contains("status 400").contains("para 100.00");
    }

    @Test
    void deve_expor_quando_a_atualizacao_impoe_o_saldo() throws Exception {
        // Preparar
        var sonda = new SondaFalsa(true);
        sonda.aceitaSaldoNaAtualizacao = true;

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar
        assertThat(achado.protegido()).isFalse();
        assertThat(achado.evidencia()).contains("status 200").contains("para 999999.00");
    }
}
