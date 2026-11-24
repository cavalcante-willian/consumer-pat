package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.obter;

import br.com.alelo.consumer.consumerpat.aplicacao.fakes.consumidor.ConsumidorPortaFake;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado.ErroConsumidorNaoEncontrado;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeConsumidores;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ObterConsumidorCasoTeste {

    @Test
    void deve_devolver_o_consumidor_com_os_cartoes_e_a_versao_quando_o_id_existe() {
        // Preparar
        var consumidor = FabricaDeConsumidores.consumidor(3, 7);
        var caso = new ObterConsumidorCaso(new ConsumidorPortaFake(consumidor));

        // Agir
        var resultado = caso.obter(new ConsumidorId(3));

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isEqualTo(consumidor);
    }

    @Test
    void deve_ser_falha_de_nao_encontrado_quando_o_id_nao_existe() {
        // Preparar
        var caso = new ObterConsumidorCaso(new ConsumidorPortaFake());

        // Agir
        var resultado = caso.obter(new ConsumidorId(3));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isEqualTo(new ErroConsumidorNaoEncontrado(new ConsumidorId(3)));
    }

    @Test
    void deve_nao_gravar_nada_quando_ler() {
        // Preparar
        var porta = new ConsumidorPortaFake(FabricaDeConsumidores.consumidor(3, 7));

        // Agir
        new ObterConsumidorCaso(porta).obter(new ConsumidorId(3));

        // Verificar
        assertThat(porta.gravados()).isEmpty();
    }

    @Test
    void deve_rodar_numa_transacao_so_de_leitura_quando_consultar_a_anotacao_do_metodo()
            throws Exception {
        // Agir
        var metodo = ObterConsumidorCaso.class.getMethod("obter", ConsumidorId.class);

        // Verificar
        var transacao = metodo.getAnnotation(Transactional.class);
        assertThat(transacao).isNotNull();
        assertThat(transacao.readOnly()).isTrue();
    }
}
