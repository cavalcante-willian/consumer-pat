package br.com.alelo.consumer.consumerpat.aplicacao.fakes;

import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.extrato.Extrato;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeCartoes;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifica o fake contra o contrato da porta real: só acrescenta, em ordem, e o que ele devolve
 * para conferência não deixa o teste alterar o que foi registrado.
 */
class ExtratoPortaFakeTeste {

    private static Extrato extrato(String produto) {
        return new Extrato("Mercado", produto, Instant.parse("2026-11-24T12:00:00Z"),
                new NumeroDeCartao(FabricaDeCartoes.NUMERO_PADRAO), TipoCartao.ALIMENTACAO,
                new Dinheiro(BigDecimal.TEN), new Dinheiro(BigDecimal.ONE));
    }

    @Test
    void deve_comecar_vazio_quando_nada_foi_registrado() {
        // Verificar
        assertThat(new ExtratoPortaFake().registrados()).isEmpty();
    }

    @Test
    void deve_guardar_na_ordem_do_registro_quando_registrar_varios() {
        // Preparar
        var porta = new ExtratoPortaFake();

        // Agir
        porta.registrar(extrato("Arroz"));
        porta.registrar(extrato("Feijão"));

        // Verificar
        assertThat(porta.registrados()).containsExactly(extrato("Arroz"), extrato("Feijão"));
    }

    @Test
    void deve_recusar_alterar_a_lista_devolvida_quando_o_teste_tenta_apagar() {
        // Preparar
        var porta = new ExtratoPortaFake();
        porta.registrar(extrato("Arroz"));

        // Agir e Verificar
        assertThatThrownBy(() -> porta.registrados().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(porta.registrados()).hasSize(1);
    }
}
