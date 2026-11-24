package br.com.alelo.consumer.consumerpat.dominio.consumidor.contato;

import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroTelefoneInvalido;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class TelefoneDeTextoTeste {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = { "11999998888|11999998888", "(11) 99999-8888|11999998888",
            "+55 11 99999-8888|5511999998888", "11.3333.4444|1133334444" })
    void deve_ser_sucesso_com_a_forma_canonica_quando_o_texto_tem_de_dez_a_quinze_digitos(
            String texto, String canonico) {
        // Agir
        var resultado = TelefoneDeTexto.converter(texto, "celular");

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isEqualTo(new Telefone(canonico));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "113333444", "5511999998888777", "1199999888a",
            "11_99999_8888", "11/99999/8888" })
    @NullSource
    void deve_ser_falha_quando_o_texto_nao_e_um_telefone(String texto) {
        // Agir
        var resultado = TelefoneDeTexto.converter(texto, "celular");

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroTelefoneInvalido.class);
    }

    @Test
    void deve_apontar_o_campo_recebido_quando_recusar() {
        // Agir
        var resultado = TelefoneDeTexto.converter("123", "telefoneResidencial");

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        var erro = assertInstanceOf(ErroTelefoneInvalido.class, falha.erro());
        assertThat(erro.camposComErro()).containsOnlyKeys("telefoneResidencial");
    }

    @Test
    void deve_nao_devolver_o_texto_recebido_quando_recusar() {
        // Agir
        var resultado = TelefoneDeTexto.converter("(11) 9999-x888", "celular");

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.toString()).doesNotContain("9999-x888");
    }
}
