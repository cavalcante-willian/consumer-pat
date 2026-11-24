package br.com.alelo.consumer.consumerpat.dominio.dinheiro;

import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroValorMonetarioInvalido;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DinheiroDeTextoTeste {

    @ParameterizedTest
    @CsvSource({ "0, 0", "10, 10", "10.5, 10.5", "10.50, 10.5", "0.000001, 0.000001",
            "-5.25, -5.25", "9999999999999, 9999999999999",
            "9999999999999.999999, 9999999999999.999999" })
    void deve_ser_sucesso_com_a_quantia_quando_o_texto_e_um_decimal_valido(String texto,
            String esperado) {
        // Agir
        var resultado = DinheiroDeTexto.converter(texto);

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isEqualTo(new Dinheiro(new BigDecimal(esperado)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "abc", "1,5", "1e3", "10.1234567", "99999999999999", ".5",
            "5.", "+5", " 5", "5 ", "--5" })
    @NullSource
    void deve_ser_falha_quando_o_texto_nao_e_um_decimal_valido(String texto) {
        // Agir
        var resultado = DinheiroDeTexto.converter(texto);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroValorMonetarioInvalido.class);
    }

    @Test
    void deve_nao_devolver_o_texto_recebido_quando_recusar() {
        // Preparar
        var texto = "segredo-1,5";

        // Agir
        var resultado = DinheiroDeTexto.converter(texto);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.toString()).doesNotContain(texto);
    }
}
