package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.atualizar;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoAtualizacaoDeConsumidor;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.SaidaDeLogFake;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.consumidor.ConsumidorPortaFake;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.DadosDoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroVersaoDivergente;
import br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado.ErroConsumidorNaoEncontrado;
import br.com.alelo.consumer.consumerpat.dominio.log.NivelDeLog;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeConsumidores;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class AtualizarConsumidorCasoTeste {

    private static final ConsumidorId ID = new ConsumidorId(1);
    private static final DadosDoConsumidor NOVOS_DADOS = FabricaDeConsumidores
            .dadosComDocumento("98765432100");

    private static DtoAtualizacaoDeConsumidor pedido(long versao) {
        return new DtoAtualizacaoDeConsumidor(ID, versao, NOVOS_DADOS);
    }

    private final SaidaDeLogFake saida = new SaidaDeLogFake();

    private AtualizarConsumidorCaso caso(ConsumidorPortaFake porta) {
        return new AtualizarConsumidorCaso(porta, porta, saida);
    }

    @Test
    void deve_trocar_os_dados_e_avancar_a_versao_quando_a_versao_e_a_atual() {
        // Preparar
        var porta = new ConsumidorPortaFake(FabricaDeConsumidores.consumidor(1, 2));

        // Agir
        var resultado = caso(porta).atualizar(pedido(2));

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        var atualizado = (Consumidor) sucesso.valor();
        assertThat(atualizado.dados()).isEqualTo(NOVOS_DADOS);
        assertThat(atualizado.versao()).isEqualTo(3);
        assertThat(porta.estado(ID)).contains(atualizado);
    }

    @Test
    void deve_manter_os_cartoes_e_o_saldo_quando_atualizar_o_cadastro() {
        // Preparar
        var antes = FabricaDeConsumidores.consumidor(1, 0);
        var porta = new ConsumidorPortaFake(antes);

        // Agir
        caso(porta).atualizar(pedido(0));

        // Verificar
        assertThat(porta.estado(ID).orElseThrow().cartoes()).isEqualTo(antes.cartoes());
    }

    @Test
    void deve_ser_falha_de_versao_divergente_e_nao_gravar_quando_a_versao_esta_velha() {
        // Preparar
        var porta = new ConsumidorPortaFake(FabricaDeConsumidores.consumidor(1, 5));

        // Agir
        var resultado = caso(porta).atualizar(pedido(4));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isEqualTo(new ErroVersaoDivergente(ID, 4));
        assertThat(porta.gravados()).isEmpty();
        assertThat(porta.estado(ID).orElseThrow().dados()).isEqualTo(FabricaDeConsumidores.dados());
    }

    @Test
    void deve_ser_falha_de_nao_encontrado_quando_o_consumidor_nao_existe() {
        // Preparar
        var porta = new ConsumidorPortaFake();

        // Agir
        var resultado = caso(porta).atualizar(pedido(0));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isEqualTo(new ErroConsumidorNaoEncontrado(ID));
    }

    @Test
    void deve_deixar_o_consumidor_como_estava_quando_a_atualizacao_e_recusada() {
        // Preparar
        var antes = FabricaDeConsumidores.consumidor(1, 5);
        var porta = new ConsumidorPortaFake(antes);

        // Agir
        caso(porta).atualizar(pedido(1));

        // Verificar
        assertThat(porta.estado(ID)).contains(antes);
    }

    @Test
    void deve_registrar_consumidor_atualizado_com_o_id_e_a_versao_nova_quando_atualizar() {
        // Preparar
        var porta = new ConsumidorPortaFake(FabricaDeConsumidores.consumidor(1, 2));

        // Agir
        caso(porta).atualizar(pedido(2));

        // Verificar: a versão que o cliente precisa reenviar na próxima atualização
        assertThat(saida.eventos()).singleElement().satisfies(evento -> {
            assertThat(evento.nivel()).isEqualTo(NivelDeLog.INFORMACAO);
            assertThat(evento.evento()).isEqualTo("consumidor.atualizado");
            assertThat(evento.valorDe("consumidorId")).contains(1L);
            assertThat(evento.valorDe("versao")).contains(3L);
        });
    }

    @Test
    void deve_nao_levar_o_cadastro_ao_log_quando_atualizar() {
        // Preparar
        var porta = new ConsumidorPortaFake(FabricaDeConsumidores.consumidor(1, 0));

        // Agir
        caso(porta).atualizar(pedido(0));

        // Verificar: nem o documento novo, nem o antigo, nem o nome
        assertThat(saida.eventos().get(0).texto()).doesNotContain("98765432100")
                .doesNotContain(FabricaDeConsumidores.DOCUMENTO_PADRAO).doesNotContain("Maria");
    }

    @Test
    void deve_registrar_conflito_de_versao_e_nao_a_atualizacao_quando_a_versao_esta_velha() {
        // Preparar
        var porta = new ConsumidorPortaFake(FabricaDeConsumidores.consumidor(1, 5));

        // Agir
        caso(porta).atualizar(pedido(4));

        // Verificar
        assertThat(saida.eventos()).singleElement().satisfies(evento -> {
            assertThat(evento.nivel()).isEqualTo(NivelDeLog.AVISO);
            assertThat(evento.evento()).isEqualTo("conflito.detectado");
            assertThat(evento.valorDe("operacao")).contains("atualizar-consumidor");
            assertThat(evento.valorDe("codigo")).contains(ErroVersaoDivergente.CODIGO);
        });
    }

    @Test
    void deve_nao_registrar_conflito_quando_o_consumidor_nao_existe() {
        // Agir
        caso(new ConsumidorPortaFake()).atualizar(pedido(0));

        // Verificar: ausência não é conflito, e a resposta 404 já diz o que houve
        assertThat(saida.eventos()).isEmpty();
    }

    @Test
    void deve_rodar_numa_transacao_quando_consultar_a_anotacao_do_metodo() throws Exception {
        // Agir
        var metodo = AtualizarConsumidorCaso.class.getMethod("atualizar",
                DtoAtualizacaoDeConsumidor.class);

        // Verificar
        assertThat(metodo.isAnnotationPresent(Transactional.class)).isTrue();
    }
}
