package br.com.alelo.consumer.consumerpat.api.comum.problema;

import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado.ErroCartaoNaoEncontrado;
import br.com.alelo.consumer.consumerpat.dominio.erros.regradenegocio.ErroSaldoInsuficiente;
import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroTipoEstabelecimentoInvalido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemaDeErroDeDominioTeste {

    private static final String INSTANCIA = "/api/v1/compras";

    @Test
    void deve_tirar_o_tipo_do_codigo_e_o_status_da_categoria_quando_montar_a_regra_de_negocio() {
        // Preparar
        var erro = new ErroSaldoInsuficiente(new Dinheiro(BigDecimal.TEN),
                new Dinheiro(BigDecimal.ONE));

        // Agir
        var problema = ProblemaDeErroDeDominio.montar(erro, INSTANCIA);

        // Verificar
        assertThat(problema.type()).isEqualTo("urn:consumer-pat:erro:CPT-REG-CRT-001");
        assertThat(problema.status()).isEqualTo(422);
        assertThat(problema.title()).isEqualTo("Regra de negócio não atendida");
        assertThat(problema.detail()).isEqualTo(erro.mensagem());
        assertThat(problema.instance()).isEqualTo(INSTANCIA);
        assertThat(problema.errors()).isEmpty();
    }

    @Test
    void deve_levar_os_campos_com_erro_do_proprio_erro_quando_montar_a_validacao() {
        // Preparar
        var erro = new ErroTipoEstabelecimentoInvalido(99);

        // Agir
        var problema = ProblemaDeErroDeDominio.montar(erro, INSTANCIA);

        // Verificar
        assertThat(problema.status()).isEqualTo(400);
        assertThat(problema.type()).isEqualTo("urn:consumer-pat:erro:CPT-VAL-CRT-001");
        assertThat(problema.errors()).isEqualTo(erro.camposComErro());
        assertThat(problema.errors()).containsOnlyKeys("tipoEstabelecimento");
    }

    @Test
    void deve_ser_404_sem_mostrar_o_numero_inteiro_quando_montar_o_nao_encontrado() {
        // Preparar
        var erro = new ErroCartaoNaoEncontrado(new NumeroDeCartao("5555000011112222"),
                TipoCartao.FARMACIA);

        // Agir
        var problema = ProblemaDeErroDeDominio.montar(erro, INSTANCIA);

        // Verificar
        assertThat(problema.status()).isEqualTo(404);
        assertThat(problema.detail()).contains("****2222").doesNotContain("5555000011112222");
        assertThat(problema.toString()).doesNotContain("5555000011112222");
    }
}
