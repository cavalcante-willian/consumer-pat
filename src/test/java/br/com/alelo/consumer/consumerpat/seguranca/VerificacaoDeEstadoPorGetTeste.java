package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VerificacaoDeEstadoPorGetTeste {

    private final VerificacaoDeEstadoPorGet verificacao = new VerificacaoDeEstadoPorGet();

    @Test
    void deve_proteger_quando_o_get_nao_muda_o_saldo() throws Exception {
        // Agir
        var achado = verificacao.verificar(new SondaFalsa(true));

        // Verificar
        assertThat(achado.chave()).isEqualTo("estado-alterado-por-get");
        assertThat(achado.protegido()).isTrue();
        assertThat(achado.evidencia()).contains("status 405").contains("para 100.00");
    }

    @Test
    void deve_expor_quando_o_get_debita_o_saldo() throws Exception {
        // Preparar
        var sonda = new SondaFalsa(true);
        sonda.compraPorGetMudaOSaldo = true;

        // Agir
        var achado = verificacao.verificar(sonda);

        // Verificar
        assertThat(achado.protegido()).isFalse();
        assertThat(achado.evidencia()).contains("status 200").contains("para 90.00");
    }
}
