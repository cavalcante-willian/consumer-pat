package br.com.alelo.consumer.consumerpat.aplicacao.fakes.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.DadosDoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.NovoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroDocumentoJaCadastrado;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroNumeroDeCartaoJaCadastrado;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroVersaoDivergente;
import br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado.ErroConsumidorNaoEncontrado;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeCartoes;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeConsumidores;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Verifica o fake contra o contrato da porta real: um fake que aceita o que a porta recusa deixa os
 * testes dos casos de uso verdes por um motivo falso.
 */
class ConsumidorPortaFakeTeste {

    private static final DadosDoConsumidor OUTROS_DADOS = FabricaDeConsumidores
            .dadosComDocumento("98765432100");

    @Test
    void deve_ser_falha_de_consumidor_nao_encontrado_quando_o_id_nao_existe() {
        // Preparar
        var porta = new ConsumidorPortaFake();

        // Agir
        var resultado = porta.buscarPorId(new ConsumidorId(9));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isEqualTo(new ErroConsumidorNaoEncontrado(new ConsumidorId(9)));
    }

    @Test
    void deve_dar_id_e_versao_zero_quando_criar_um_consumidor() {
        // Preparar
        var porta = new ConsumidorPortaFake();

        // Agir
        var resultado = porta.criar(FabricaDeConsumidores.novo());

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        var criado = (Consumidor) sucesso.valor();
        assertThat(criado.id()).isEqualTo(new ConsumidorId(1));
        assertThat(criado.versao()).isZero();
        assertThat(porta.estado(criado.id())).contains(criado);
    }

    @Test
    void deve_dar_ids_diferentes_quando_criar_dois_consumidores() {
        // Preparar
        var porta = new ConsumidorPortaFake();
        var segundo = new NovoConsumidor(OUTROS_DADOS,
                new br.com.alelo.consumer.consumerpat.dominio.consumidor.CartoesDoConsumidor(
                        List.of()));

        // Agir
        porta.criar(FabricaDeConsumidores.novo());
        porta.criar(segundo);

        // Verificar
        assertThat(porta.gravados()).extracting(Consumidor::id).containsExactly(new ConsumidorId(1),
                new ConsumidorId(2));
    }

    @Test
    void deve_ser_falha_de_documento_repetido_e_nao_gravar_quando_o_documento_ja_existe() {
        // Preparar
        var porta = new ConsumidorPortaFake(FabricaDeConsumidores.consumidor(1, 0));
        var comOMesmoDocumento = new NovoConsumidor(FabricaDeConsumidores.dados(),
                new br.com.alelo.consumer.consumerpat.dominio.consumidor.CartoesDoConsumidor(
                        List.of()));

        // Agir
        var resultado = porta.criar(comOMesmoDocumento);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroDocumentoJaCadastrado.class);
        assertThat(porta.gravados()).isEmpty();
    }

    @Test
    void deve_ser_falha_de_numero_repetido_e_nao_gravar_quando_o_numero_do_cartao_ja_existe() {
        // Preparar
        var porta = new ConsumidorPortaFake(FabricaDeConsumidores.consumidor(1, 0));
        var comOMesmoNumero = new NovoConsumidor(OUTROS_DADOS,
                new br.com.alelo.consumer.consumerpat.dominio.consumidor.CartoesDoConsumidor(
                        List.of(FabricaDeCartoes.doTipo(TipoCartao.FARMACIA).construir())));

        // Agir
        var resultado = porta.criar(comOMesmoNumero);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isEqualTo(new ErroNumeroDeCartaoJaCadastrado(
                new NumeroDeCartao(FabricaDeCartoes.NUMERO_PADRAO)));
        assertThat(porta.gravados()).isEmpty();
    }

    @Test
    void deve_trocar_so_os_dados_e_avancar_a_versao_quando_atualizar() {
        // Preparar
        var atual = FabricaDeConsumidores.consumidor(1, 4);
        var porta = new ConsumidorPortaFake(atual);

        // Agir
        var resultado = porta.atualizar(atual.comDados(OUTROS_DADOS));

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        var atualizado = (Consumidor) sucesso.valor();
        assertThat(atualizado.dados()).isEqualTo(OUTROS_DADOS);
        assertThat(atualizado.versao()).isEqualTo(5);
        assertThat(atualizado.cartoes()).isEqualTo(atual.cartoes());
    }

    @Test
    void deve_ignorar_o_saldo_que_veio_no_consumidor_quando_atualizar() {
        // Preparar
        var atual = FabricaDeConsumidores.consumidor(1, 0);
        var porta = new ConsumidorPortaFake(atual);
        var comSaldoVelho = new Consumidor(atual.id(), OUTROS_DADOS,
                new br.com.alelo.consumer.consumerpat.dominio.consumidor.CartoesDoConsumidor(
                        List.of(FabricaDeCartoes.doTipo(TipoCartao.ALIMENTACAO).comSaldo("999")
                                .construir())),
                0);

        // Agir
        porta.atualizar(comSaldoVelho);

        // Verificar
        var gravado = porta.estado(atual.id()).orElseThrow();
        assertThat(gravado.cartoes().cartoes().get(0).saldo())
                .isEqualTo(atual.cartoes().cartoes().get(0).saldo());
        assertThat(gravado.cartoes().cartoes().get(0).saldo())
                .isNotEqualTo(new Dinheiro(new java.math.BigDecimal("999")));
    }

    @Test
    void deve_ser_falha_de_versao_divergente_quando_a_versao_nao_e_a_gravada() {
        // Preparar
        var atual = FabricaDeConsumidores.consumidor(1, 4);
        var porta = new ConsumidorPortaFake(atual);

        // Agir
        var resultado = porta.atualizar(FabricaDeConsumidores.consumidor(1, 3));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isEqualTo(new ErroVersaoDivergente(new ConsumidorId(1), 3));
        assertThat(porta.gravados()).isEmpty();
    }

    @Test
    void deve_ser_falha_de_nao_encontrado_quando_atualizar_um_consumidor_ausente() {
        // Preparar
        var porta = new ConsumidorPortaFake();

        // Agir
        var resultado = porta.atualizar(FabricaDeConsumidores.consumidor(7, 0));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroConsumidorNaoEncontrado.class);
    }
}
