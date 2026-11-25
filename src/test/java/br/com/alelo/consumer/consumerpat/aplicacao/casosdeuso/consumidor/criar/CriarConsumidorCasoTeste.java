package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.criar;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoNovoConsumidor;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.SaidaDeLogFake;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.consumidor.ConsumidorPortaFake;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroDocumentoJaCadastrado;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroNumeroDeCartaoJaCadastrado;
import br.com.alelo.consumer.consumerpat.dominio.log.NivelDeLog;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeConsumidores;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CriarConsumidorCasoTeste {

    private static final NumeroDeCartao ALIMENTACAO = new NumeroDeCartao("5555000011112222");
    private static final NumeroDeCartao FARMACIA = new NumeroDeCartao("5555000011113333");

    private final SaidaDeLogFake saida = new SaidaDeLogFake();

    private static DtoNovoConsumidor pedido(Map<TipoCartao, NumeroDeCartao> numeros) {
        return new DtoNovoConsumidor(FabricaDeConsumidores.dados(), numeros);
    }

    @Test
    void deve_gravar_o_consumidor_com_id_e_versao_zero_quando_o_cadastro_e_novo() {
        // Preparar
        var porta = new ConsumidorPortaFake();
        var caso = new CriarConsumidorCaso(porta, saida);

        // Agir
        var resultado = caso.criar(pedido(Map.of()));

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        var criado = (Consumidor) sucesso.valor();
        assertThat(criado.id()).isEqualTo(new ConsumidorId(1));
        assertThat(criado.versao()).isZero();
        assertThat(criado.dados()).isEqualTo(FabricaDeConsumidores.dados());
        assertThat(porta.gravados()).containsExactly(criado);
    }

    @Test
    void deve_criar_os_cartoes_com_saldo_zero_quando_o_pedido_traz_numeros() {
        // Preparar
        var porta = new ConsumidorPortaFake();
        var caso = new CriarConsumidorCaso(porta, saida);

        // Agir
        var resultado = caso.criar(
                pedido(Map.of(TipoCartao.ALIMENTACAO, ALIMENTACAO, TipoCartao.FARMACIA, FARMACIA)));

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        var criado = (Consumidor) sucesso.valor();
        assertThat(criado.cartoes().cartoes()).hasSize(2)
                .allMatch(cartao -> cartao.saldo().equals(Dinheiro.ZERO));
    }

    @Test
    void deve_ser_falha_de_documento_repetido_e_nao_gravar_quando_o_documento_ja_existe() {
        // Preparar
        var porta = new ConsumidorPortaFake(FabricaDeConsumidores.consumidor(1, 0));
        var caso = new CriarConsumidorCaso(porta, saida);

        // Agir
        var resultado = caso.criar(pedido(Map.of()));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroDocumentoJaCadastrado.class);
        assertThat(porta.gravados()).isEmpty();
    }

    @Test
    void deve_ser_falha_de_numero_repetido_e_nao_gravar_quando_o_cartao_ja_existe() {
        // Preparar
        var porta = new ConsumidorPortaFake(FabricaDeConsumidores.consumidor(1, 0));
        var caso = new CriarConsumidorCaso(porta, saida);
        var outroDocumento = new DtoNovoConsumidor(
                FabricaDeConsumidores.dadosComDocumento("98765432100"),
                Map.of(TipoCartao.FARMACIA, new NumeroDeCartao("5555000011112222")));

        // Agir
        var resultado = caso.criar(outroDocumento);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroNumeroDeCartaoJaCadastrado.class);
        assertThat(porta.gravados()).isEmpty();
    }

    @Test
    void deve_registrar_consumidor_criado_com_o_id_quando_o_cadastro_e_efetivado() {
        // Preparar
        var caso = new CriarConsumidorCaso(new ConsumidorPortaFake(), saida);

        // Agir
        caso.criar(pedido(Map.of()));

        // Verificar
        assertThat(saida.eventos()).singleElement().satisfies(evento -> {
            assertThat(evento.nivel()).isEqualTo(NivelDeLog.INFORMACAO);
            assertThat(evento.evento()).isEqualTo("consumidor.criado");
            assertThat(evento.valorDe("consumidorId")).contains(1L);
        });
    }

    @Test
    void deve_nao_levar_dado_pessoal_ao_log_quando_o_cadastro_e_efetivado() {
        // Preparar
        var caso = new CriarConsumidorCaso(new ConsumidorPortaFake(), saida);

        // Agir
        caso.criar(pedido(Map.of(TipoCartao.ALIMENTACAO, ALIMENTACAO)));

        // Verificar: só o id identifica o consumidor
        assertThat(saida.eventos().get(0).texto())
                .doesNotContain(FabricaDeConsumidores.DOCUMENTO_PADRAO).doesNotContain("Maria")
                .doesNotContain("11999998888").doesNotContain("5555000011112222");
    }

    @Test
    void deve_registrar_conflito_com_o_codigo_e_nao_o_cadastro_quando_o_documento_ja_existe() {
        // Preparar
        var porta = new ConsumidorPortaFake(FabricaDeConsumidores.consumidor(1, 0));
        var caso = new CriarConsumidorCaso(porta, saida);

        // Agir
        caso.criar(pedido(Map.of()));

        // Verificar
        assertThat(saida.eventos()).singleElement().satisfies(evento -> {
            assertThat(evento.nivel()).isEqualTo(NivelDeLog.AVISO);
            assertThat(evento.evento()).isEqualTo("conflito.detectado");
            assertThat(evento.valorDe("operacao")).contains("criar-consumidor");
            assertThat(evento.valorDe("codigo")).contains(ErroDocumentoJaCadastrado.CODIGO);
        });
    }

    @Test
    void deve_registrar_conflito_de_numero_sem_o_numero_quando_o_cartao_ja_existe() {
        // Preparar
        var porta = new ConsumidorPortaFake(FabricaDeConsumidores.consumidor(1, 0));
        var caso = new CriarConsumidorCaso(porta, saida);
        var outroDocumento = new DtoNovoConsumidor(
                FabricaDeConsumidores.dadosComDocumento("98765432100"),
                Map.of(TipoCartao.FARMACIA, new NumeroDeCartao("5555000011112222")));

        // Agir
        caso.criar(outroDocumento);

        // Verificar: o código identifica a regra, e o valor que colidiu fica fora do log
        assertThat(saida.eventos()).singleElement().satisfies(evento -> {
            assertThat(evento.valorDe("codigo")).contains(ErroNumeroDeCartaoJaCadastrado.CODIGO);
            assertThat(evento.texto()).doesNotContain("5555000011112222")
                    .doesNotContain("98765432100");
        });
    }

    @Test
    void deve_rodar_numa_transacao_quando_consultar_a_anotacao_do_metodo() throws Exception {
        // Agir
        var metodo = CriarConsumidorCaso.class.getMethod("criar", DtoNovoConsumidor.class);

        // Verificar
        assertThat(metodo.isAnnotationPresent(Transactional.class)).isTrue();
    }
}
