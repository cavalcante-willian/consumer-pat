package br.com.alelo.consumer.consumerpat.aplicacao.fakes;

import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado.ErroCartaoNaoEncontrado;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeCartoes;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Verifica o fake contra o contrato da porta real: um fake que aceita o que a porta recusa deixa os
 * testes dos casos de uso verdes por um motivo falso.
 */
class CartaoPortaFakeTeste {

    private static final NumeroDeCartao NUMERO = new NumeroDeCartao(FabricaDeCartoes.NUMERO_PADRAO);

    @Test
    void deve_ser_falha_de_cartao_nao_encontrado_quando_o_cartao_nao_existe() {
        // Preparar
        var porta = new CartaoPortaFake();

        // Agir
        var resultado = porta.buscarPorNumeroETipoParaAtualizacao(NUMERO, TipoCartao.ALIMENTACAO);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro())
                .isEqualTo(new ErroCartaoNaoEncontrado(NUMERO, TipoCartao.ALIMENTACAO));
    }

    @Test
    void deve_ser_falha_quando_existe_cartao_com_o_mesmo_numero_e_outro_tipo() {
        // Preparar
        var porta = new CartaoPortaFake(FabricaDeCartoes.doTipo(TipoCartao.FARMACIA).construir());

        // Agir
        var resultado = porta.buscarPorNumeroETipoParaAtualizacao(NUMERO, TipoCartao.ALIMENTACAO);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroCartaoNaoEncontrado.class);
    }

    @Test
    void deve_devolver_o_cartao_quando_o_numero_e_o_tipo_existem() {
        // Preparar
        var cartao = FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).construir();
        var porta = new CartaoPortaFake(cartao);

        // Agir
        var resultado = porta.buscarPorNumeroETipoParaAtualizacao(NUMERO, TipoCartao.ALIMENTACAO);

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isEqualTo(cartao);
    }

    @Test
    void deve_substituir_o_cartao_quando_salvar_um_com_o_mesmo_numero_e_tipo() {
        // Preparar
        var porta = new CartaoPortaFake(
                FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).construir());
        var novo = FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).comSaldo("7.00").construir();

        // Agir
        porta.salvar(novo);

        // Verificar
        assertThat(porta.estado(NUMERO, TipoCartao.ALIMENTACAO)).contains(novo);
        assertThat(porta.estado(NUMERO, TipoCartao.ALIMENTACAO).orElseThrow().saldo())
                .isEqualTo(new Dinheiro(new BigDecimal("7.00")));
    }

    @Test
    void deve_nao_contar_os_cartoes_iniciais_como_gravacoes_quando_listar_os_gravados() {
        // Preparar
        var porta = new CartaoPortaFake(
                FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).construir());

        // Verificar
        assertThat(porta.gravados()).isEmpty();
    }

    @Test
    void deve_contar_a_gravacao_quando_salvar() {
        // Preparar
        var existente = FabricaDeCartoes.doTipo(TipoCartao.COMBUSTIVEL).construir();
        var porta = new CartaoPortaFake(existente);
        var novo = FabricaDeCartoes.doTipo(TipoCartao.COMBUSTIVEL).comSaldo("5.00").construir();

        // Agir
        porta.salvar(novo);

        // Verificar
        assertThat(porta.gravados()).containsExactly(novo);
    }

    @Test
    void deve_lancar_sem_gravar_quando_salvar_um_cartao_que_nao_existe() {
        // Preparar
        var porta = new CartaoPortaFake();
        var cartao = FabricaDeCartoes.doTipo(TipoCartao.COMBUSTIVEL).construir();

        // Agir e Verificar
        assertThatThrownBy(() -> porta.salvar(cartao)).isInstanceOf(IllegalStateException.class)
                .hasMessageNotContaining(FabricaDeCartoes.NUMERO_PADRAO);
        assertThat(porta.gravados()).isEmpty();
        assertThat(porta.estado(NUMERO, TipoCartao.COMBUSTIVEL)).isEmpty();
    }
}
