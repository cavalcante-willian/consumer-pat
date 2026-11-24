package br.com.alelo.consumer.consumerpat.dominio.consumidor.documento;

import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroDocumentoInvalido;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DocumentoDeTextoTeste {

    @ParameterizedTest
    @CsvSource(delimiter = '|',
            value = { "12345678901|12345678901", "123.456.789-01|12345678901",
                    "123 456 789 01|12345678901", "12.345.678/0001-95|12345678000195",
                    "12345678000195|12345678000195" })
    void deve_ser_sucesso_com_a_forma_canonica_quando_o_texto_tem_onze_ou_catorze_digitos(
            String texto, String canonico) {
        // Agir
        var resultado = DocumentoDeTexto.converter(texto);

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isEqualTo(new Documento(canonico));
    }

    @Test
    void deve_dar_o_mesmo_documento_quando_o_mesmo_numero_e_escrito_de_duas_maneiras() {
        // Agir
        var comPontuacao = DocumentoDeTexto.converter("123.456.789-01");
        var semPontuacao = DocumentoDeTexto.converter("12345678901");

        // Verificar
        assertThat(comPontuacao).isEqualTo(semPontuacao);
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "1234567890", "123456789012", "1234567890a", "123_456_789_01",
            "123\t456 789 01", "123,456,789-01" })
    @NullSource
    void deve_ser_falha_quando_o_texto_nao_e_um_documento(String texto) {
        // Agir
        var resultado = DocumentoDeTexto.converter(texto);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroDocumentoInvalido.class);
    }

    @Test
    void deve_nao_devolver_o_texto_recebido_quando_recusar() {
        // Agir
        var resultado = DocumentoDeTexto.converter("123.456.789-0x");

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.toString()).doesNotContain("123.456").doesNotContain("789-0x");
    }
}
