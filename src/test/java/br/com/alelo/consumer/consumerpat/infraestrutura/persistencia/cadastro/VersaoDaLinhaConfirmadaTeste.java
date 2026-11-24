package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cadastro;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.erros.conflito.ErroVersaoDivergente;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeConsumidores;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ConsumidoresDeTeste;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class VersaoDaLinhaConfirmadaTeste {

    @Test
    void deve_ser_sucesso_com_a_mesma_linha_quando_a_versao_e_a_da_linha() {
        // Preparar
        var linha = ConsumidoresDeTeste.linha();
        linha.setVersao(3L);

        // Agir
        var resultado = VersaoDaLinhaConfirmada.exigir(linha,
                FabricaDeConsumidores.consumidor(1, 3));

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isSameAs(linha);
    }

    @ParameterizedTest
    @ValueSource(longs = { 0, 2, 4, 100 })
    void deve_ser_falha_de_versao_divergente_quando_a_versao_nao_e_a_da_linha(long informada) {
        // Preparar
        var linha = ConsumidoresDeTeste.linha();
        linha.setVersao(3L);

        // Agir
        var resultado = VersaoDaLinhaConfirmada.exigir(linha,
                FabricaDeConsumidores.consumidor(5, informada));

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro())
                .isEqualTo(new ErroVersaoDivergente(new ConsumidorId(5), informada));
    }

    @Test
    void deve_aceitar_a_versao_zero_quando_a_linha_acabou_de_ser_gravada() {
        // Preparar
        var linha = ConsumidoresDeTeste.linha();
        linha.setVersao(0L);

        // Agir
        var resultado = VersaoDaLinhaConfirmada.exigir(linha,
                FabricaDeConsumidores.consumidor(1, 0));

        // Verificar
        assertInstanceOf(Sucesso.class, resultado);
    }
}
