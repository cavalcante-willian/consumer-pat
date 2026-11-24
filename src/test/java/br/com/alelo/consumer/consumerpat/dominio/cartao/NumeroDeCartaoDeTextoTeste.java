package br.com.alelo.consumer.consumerpat.dominio.cartao;

import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroNumeroDeCartaoInvalido;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class NumeroDeCartaoDeTextoTeste {

    @ParameterizedTest
    @CsvSource(delimiter = '|',
            value = { "5555000011112222|5555000011112222", "5555 0000 1111 2222|5555000011112222",
                    "5555-0000-1111-2222|5555000011112222", "5555 0000-1111 2222|5555000011112222",
                    "1234567890123|1234567890123", "1234567890123456789|1234567890123456789" })
    void deve_ser_sucesso_com_a_forma_canonica_quando_o_texto_tem_de_treze_a_dezenove_digitos(
            String texto, String canonico) {
        // Agir
        var resultado = NumeroDeCartaoDeTexto.converter(texto);

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isEqualTo(new NumeroDeCartao(canonico));
    }

    @Test
    void deve_dar_o_mesmo_numero_quando_o_mesmo_cartao_e_escrito_de_duas_maneiras() {
        // Agir
        var comEspacos = NumeroDeCartaoDeTexto.converter("5555 0000 1111 2222");
        var semSeparador = NumeroDeCartaoDeTexto.converter("5555000011112222");

        // Verificar
        assertThat(comEspacos).isEqualTo(semSeparador);
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "-", "123456789012", "12345678901234567890",
            "555500001111222a", "5555.0000.1111.2222", "5555_0000_1111_2222", "5555/0000/1111/2222",
            "5555\t0000 1111 22" })
    @NullSource
    void deve_ser_falha_quando_o_texto_nao_e_um_numero_de_cartao(String texto) {
        // Agir
        var resultado = NumeroDeCartaoDeTexto.converter(texto);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroNumeroDeCartaoInvalido.class);
    }

    @Test
    void deve_nao_devolver_o_texto_recebido_quando_recusar() {
        // Preparar
        var quaseUmCartao = "5555 0000 1111 222x";

        // Agir
        var resultado = NumeroDeCartaoDeTexto.converter(quaseUmCartao);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.toString()).doesNotContain("5555").doesNotContain("222x");
        assertThat(((ErroNumeroDeCartaoInvalido) falha.erro()).mensagem()).doesNotContain("5555");
    }
}
