package br.com.alelo.consumer.consumerpat.dominio.consumidor.contato;

import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroTelefoneInvalido;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class TelefoneOpcionalDeTextoTeste {

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "   " })
    @NullSource
    void deve_ser_sucesso_vazio_quando_o_telefone_nao_foi_informado(String texto) {
        // Agir
        var resultado = TelefoneOpcionalDeTexto.converter(texto, "telefone");

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isEqualTo(Optional.empty());
    }

    @Test
    void deve_ser_sucesso_com_o_telefone_quando_o_texto_e_valido() {
        // Agir
        var resultado = TelefoneOpcionalDeTexto.converter("(11) 3333-4444", "telefone");

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isEqualTo(Optional.of(new Telefone("1133334444")));
    }

    @Test
    void deve_ser_falha_quando_o_telefone_foi_informado_e_esta_mal_escrito() {
        // Agir
        var resultado = TelefoneOpcionalDeTexto.converter("123", "telefone");

        // Verificar: um telefone mal escrito não é ignorado em silêncio
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroTelefoneInvalido.class);
    }

    @Test
    void deve_apontar_o_campo_recebido_quando_recusar() {
        // Agir
        var resultado = TelefoneOpcionalDeTexto.converter("123", "telefoneResidencial");

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        var erro = assertInstanceOf(ErroTelefoneInvalido.class, falha.erro());
        assertThat(erro.camposComErro()).containsOnlyKeys("telefoneResidencial");
    }
}
