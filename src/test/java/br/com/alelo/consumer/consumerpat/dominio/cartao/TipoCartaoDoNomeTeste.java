package br.com.alelo.consumer.consumerpat.dominio.cartao;

import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroTipoDeCartaoInvalido;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class TipoCartaoDoNomeTeste {

    @ParameterizedTest
    @EnumSource(TipoCartao.class)
    void deve_ser_sucesso_com_o_tipo_quando_o_nome_e_o_do_enum(TipoCartao tipo) {
        // Agir
        var resultado = TipoCartaoDoNome.converter(tipo.name());

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isEqualTo(tipo);
    }

    @ParameterizedTest
    @ValueSource(
            strings = { "", " ", "alimentacao", "Alimentacao", "1", "REFEICAO", "ALIMENTACAO " })
    @NullSource
    void deve_ser_falha_quando_o_nome_nao_e_o_de_um_tipo(String nome) {
        // Agir
        var resultado = TipoCartaoDoNome.converter(nome);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        assertThat(falha.erro()).isInstanceOf(ErroTipoDeCartaoInvalido.class);
    }

    @ParameterizedTest
    @ValueSource(strings = { "REFEICAO-SECRETA" })
    void deve_nao_devolver_o_texto_recebido_quando_recusar(String nome) {
        // Agir
        var falha = assertInstanceOf(Falha.class, TipoCartaoDoNome.converter(nome));

        // Verificar
        assertThat(falha.toString()).doesNotContain("SECRETA");
    }
}
