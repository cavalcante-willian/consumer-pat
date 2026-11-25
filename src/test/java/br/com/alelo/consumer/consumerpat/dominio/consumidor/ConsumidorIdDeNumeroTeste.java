package br.com.alelo.consumer.consumerpat.dominio.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroConsumidorIdInvalido;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ConsumidorIdDeNumeroTeste {

    @ParameterizedTest
    @ValueSource(longs = { 1, 42, Long.MAX_VALUE })
    void deve_ser_sucesso_com_o_id_quando_o_numero_e_positivo(long numero) {
        // Agir
        var resultado = ConsumidorIdDeNumero.converter(numero);

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isEqualTo(new ConsumidorId(numero));
    }

    @ParameterizedTest
    @ValueSource(longs = { 0, -1, Long.MIN_VALUE })
    void deve_ser_falha_de_validacao_sem_lancar_quando_o_numero_nao_e_positivo(long numero) {
        // Agir
        var resultado = ConsumidorIdDeNumero.converter(numero);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroConsumidorIdInvalido.class);
    }
}
