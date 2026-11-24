package br.com.alelo.consumer.consumerpat.dominio.consumidor.contato;

import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroEmailInvalido;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class EmailDeTextoTeste {

    @Test
    void deve_ser_sucesso_sem_espaco_nas_pontas_quando_o_texto_e_um_email() {
        // Agir
        var resultado = EmailDeTexto.converter("  maria@exemplo.com  ");

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isEqualTo(new Email("maria@exemplo.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "maria", "maria@exemplo", "maria souza@exemplo.com" })
    @NullSource
    void deve_ser_falha_quando_o_texto_nao_e_um_email(String texto) {
        // Agir
        var resultado = EmailDeTexto.converter(texto);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroEmailInvalido.class);
    }

    @Test
    void deve_nao_devolver_o_texto_recebido_quando_recusar() {
        // Agir
        var resultado = EmailDeTexto.converter("maria-sem-arroba");

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.toString()).doesNotContain("maria-sem-arroba");
    }
}
