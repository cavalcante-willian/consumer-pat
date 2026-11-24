package br.com.alelo.consumer.consumerpat.dominio.consumidor.endereco;

import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroCepInvalido;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CepDeTextoTeste {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = { "01001000|01001000", "01001-000|01001000" })
    void deve_ser_sucesso_com_a_forma_canonica_quando_o_texto_tem_oito_digitos(String texto,
            String canonico) {
        // Agir
        var resultado = CepDeTexto.converter(texto);

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isEqualTo(new Cep(canonico));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "0100100", "010010000", "0100100a", "01001 000" })
    @NullSource
    void deve_ser_falha_quando_o_texto_nao_e_um_cep(String texto) {
        // Agir
        var resultado = CepDeTexto.converter(texto);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroCepInvalido.class);
    }
}
