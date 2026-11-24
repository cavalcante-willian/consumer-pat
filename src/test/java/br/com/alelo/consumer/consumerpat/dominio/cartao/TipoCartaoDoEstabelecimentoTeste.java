package br.com.alelo.consumer.consumerpat.dominio.cartao;

import br.com.alelo.consumer.consumerpat.dominio.erros.validacao.ErroTipoEstabelecimentoInvalido;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class TipoCartaoDoEstabelecimentoTeste {

    @ParameterizedTest
    @CsvSource({ "1, ALIMENTACAO", "2, FARMACIA", "3, COMBUSTIVEL" })
    void deve_ser_sucesso_com_o_tipo_quando_o_codigo_corresponde_a_um_estabelecimento(int codigo,
            TipoCartao esperado) {
        // Agir
        var resultado = TipoCartaoDoEstabelecimento.converter(codigo);

        // Verificar
        var sucesso = assertInstanceOf(Sucesso.class, resultado);
        assertThat(sucesso.valor()).isEqualTo(esperado);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 4, 9, 99, -1, Integer.MAX_VALUE, Integer.MIN_VALUE })
    void deve_ser_falha_com_o_codigo_recebido_quando_nao_existe_o_tipo_de_estabelecimento(
            int codigo) {
        // Agir
        var resultado = TipoCartaoDoEstabelecimento.converter(codigo);

        // Verificar
        var falha = assertInstanceOf(Falha.class, resultado);
        var erro = assertInstanceOf(ErroTipoEstabelecimentoInvalido.class, falha.erro());
        assertThat(erro.codigoRecebido()).isEqualTo(codigo);
    }
}
