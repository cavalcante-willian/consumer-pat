package br.com.alelo.consumer.consumerpat.seguranca;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VerificacaoDeValorNegativoTeste {

    private final VerificacaoDeValorNegativo verificacao = new VerificacaoDeValorNegativo();

    @Test
    void deve_proteger_quando_o_sistema_recusa_a_compra_de_valor_negativo() throws Exception {
        // Agir
        var achado = verificacao.verificar(new SondaFalsa(true));

        // Verificar
        assertThat(achado.chave()).isEqualTo("valor-negativo");
        assertThat(achado.protegido()).isTrue();
        assertThat(achado.evidencia()).contains("status 400").contains("para 100.00");
    }

    @Test
    void deve_expor_quando_a_compra_de_valor_negativo_soma_ao_saldo() throws Exception {
        // Agir
        var achado = verificacao.verificar(new SondaFalsa(false));

        // Verificar: menos cinquenta de compra vira mais cinquenta de saldo
        assertThat(achado.protegido()).isFalse();
        assertThat(achado.evidencia()).contains("status 201").contains("para 150.00");
    }
}
