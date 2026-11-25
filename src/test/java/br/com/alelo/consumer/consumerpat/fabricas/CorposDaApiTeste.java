package br.com.alelo.consumer.consumerpat.fabricas;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CorposDaApiTeste {

    @Test
    void deve_levar_o_documento_e_o_cartao_pelo_nome_do_tipo_quando_montar_o_cadastro() {
        // Agir
        var corpo = CorposDaApi.cadastro("70000000001", TipoCartao.FARMACIA, "7000000000000001");

        // Verificar
        assertThat(JsonPath.<String>read(corpo, "$.dados.documento")).isEqualTo("70000000001");
        assertThat(JsonPath.<String>read(corpo, "$.numerosCartoes.FARMACIA"))
                .isEqualTo("7000000000000001");
    }

    @Test
    void deve_levar_o_codigo_do_estabelecimento_e_o_valor_quando_montar_o_credito() {
        // Agir
        var corpo = CorposDaApi.credito("7000000000000001", TipoCartao.COMBUSTIVEL, "12.50");

        // Verificar
        assertThat(JsonPath.<String>read(corpo, "$.numeroCartao")).isEqualTo("7000000000000001");
        assertThat(JsonPath.<Integer>read(corpo, "$.tipoEstabelecimento")).isEqualTo(3);
        assertThat(JsonPath.<Double>read(corpo, "$.valor")).isEqualTo(12.5);
    }

    @Test
    void deve_levar_o_codigo_do_estabelecimento_e_o_valor_quando_montar_a_compra() {
        // Agir
        var corpo = CorposDaApi.compra(TipoCartao.ALIMENTACAO, "7000000000000001", "10.00");

        // Verificar
        assertThat(JsonPath.<Integer>read(corpo, "$.tipoEstabelecimento")).isEqualTo(1);
        assertThat(JsonPath.<String>read(corpo, "$.numeroCartao")).isEqualTo("7000000000000001");
        assertThat(JsonPath.<Double>read(corpo, "$.valor")).isEqualTo(10.0);
    }
}
