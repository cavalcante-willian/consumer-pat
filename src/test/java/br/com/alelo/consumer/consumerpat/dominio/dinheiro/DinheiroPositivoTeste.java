package br.com.alelo.consumer.consumerpat.dominio.dinheiro;

import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroValorNaoPositivo;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DinheiroPositivoTeste {

    @ParameterizedTest
    @ValueSource(strings = { "0.000001", "0.01", "1", "9999999999999.999999" })
    void deve_devolver_a_mesma_quantia_quando_e_maior_que_zero(String valor) {
        // Preparar
        var quantia = new Dinheiro(new BigDecimal(valor));

        // Agir
        var resultado = DinheiroPositivo.exigir(quantia);

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isEqualTo(quantia);
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", "-0.000001", "-1", "-9999999999999.999999" })
    void deve_ser_falha_quando_e_zero_ou_negativa(String valor) {
        // Preparar
        var quantia = new Dinheiro(new BigDecimal(valor));

        // Agir
        var resultado = DinheiroPositivo.exigir(quantia);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroValorNaoPositivo.class);
    }

    @ParameterizedTest
    @NullSource
    void deve_ser_falha_quando_a_quantia_e_nula(Dinheiro quantia) {
        // Agir
        var resultado = DinheiroPositivo.exigir(quantia);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroValorNaoPositivo.class);
    }
}
