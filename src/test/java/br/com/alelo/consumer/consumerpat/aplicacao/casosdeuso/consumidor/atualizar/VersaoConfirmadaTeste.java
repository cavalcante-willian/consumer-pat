package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.atualizar;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroVersaoDivergente;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeConsumidores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class VersaoConfirmadaTeste {

    @Test
    void deve_ser_sucesso_com_o_mesmo_consumidor_quando_a_versao_e_a_atual() {
        // Preparar
        var atual = FabricaDeConsumidores.consumidor(5, 3);

        // Agir
        var resultado = VersaoConfirmada.exigir(atual, 3);

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isSameAs(atual);
    }

    @ParameterizedTest
    @ValueSource(longs = { 0, 2, 4, 100 })
    void deve_ser_falha_de_versao_divergente_quando_a_versao_nao_e_a_atual(long informada) {
        // Preparar
        var atual = FabricaDeConsumidores.consumidor(5, 3);

        // Agir
        var resultado = VersaoConfirmada.exigir(atual, informada);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro())
                .isEqualTo(new ErroVersaoDivergente(new ConsumidorId(5), informada));
    }

    @Test
    void deve_aceitar_a_versao_zero_quando_o_consumidor_acabou_de_ser_gravado() {
        // Agir
        var resultado = VersaoConfirmada.exigir(FabricaDeConsumidores.consumidor(1, 0), 0);

        // Verificar
        assertInstanceOf(Sucesso.class, resultado);
    }
}
