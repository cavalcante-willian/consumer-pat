package br.com.alelo.consumer.consumerpat.medicao;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CorposDoLegadoTeste {

    @Test
    void deve_numerar_tres_cartoes_seguidos_por_consumidor_na_ordem_do_enum_quando_numerar() {
        // Agir e Verificar
        assertThat(CorposDoLegado.documento(7)).isEqualTo(1_000_007);
        assertThat(CorposDoLegado.numeroDoCartao(7, TipoCartao.ALIMENTACAO)).isEqualTo(100_000_021);
        assertThat(CorposDoLegado.numeroDoCartao(7, TipoCartao.FARMACIA)).isEqualTo(100_000_022);
        assertThat(CorposDoLegado.numeroDoCartao(7, TipoCartao.COMBUSTIVEL)).isEqualTo(100_000_023);
    }

    @Test
    void deve_levar_os_tres_cartoes_com_saldo_zero_quando_montar_o_cadastro_da_base() {
        // Agir
        var corpo = CorposDoLegado.cadastro(7);

        // Verificar
        assertThat(JsonPath.<Integer>read(corpo, "$.documentNumber")).isEqualTo(1_000_007);
        assertThat(JsonPath.<Integer>read(corpo, "$.foodCardNumber")).isEqualTo(100_000_021);
        assertThat(JsonPath.<Double>read(corpo, "$.drugstoreCardBalance")).isZero();
    }

    @Test
    void deve_levar_o_saldo_so_no_cartao_de_farmacia_quando_montar_o_cadastro_da_disputa() {
        // Agir
        var corpo = CorposDoLegado.cadastroDeFarmacia(900_000_001, 900_000_002, "1000.00");

        // Verificar
        assertThat(JsonPath.<Integer>read(corpo, "$.drugstoreNumber")).isEqualTo(900_000_002);
        assertThat(JsonPath.<Double>read(corpo, "$.drugstoreCardBalance")).isEqualTo(1000.0);
        assertThat(JsonPath.<Integer>read(corpo, "$.foodCardNumber")).isZero();
    }

    @Test
    void deve_levar_o_nome_hostil_inteiro_e_o_json_valido_quando_montar_o_cadastro_com_nome() {
        // Preparar
        var hostil = "Robert\"); DROP TABLE x;--\\ \u0001";

        // Agir
        var corpo = CorposDoLegado.cadastroComNome(hostil, 800_000_002, 800_000_003);

        // Verificar
        assertThat(JsonPath.<String>read(corpo, "$.name")).isEqualTo(hostil);
        assertThat(JsonPath.<Integer>read(corpo, "$.documentNumber")).isEqualTo(800_000_002);
        assertThat(JsonPath.<Integer>read(corpo, "$.drugstoreNumber")).isEqualTo(800_000_003);
    }

    @Test
    void deve_levar_o_id_e_o_saldo_quando_montar_a_atualizacao_com_saldo() {
        // Agir
        var corpo = CorposDoLegado.atualizacaoComSaldo(8, 900_000_001, "999999.00");

        // Verificar: o legado grava o objeto recebido inteiro, e o saldo vai junto
        assertThat(JsonPath.<Integer>read(corpo, "$.id")).isEqualTo(8);
        assertThat(JsonPath.<Integer>read(corpo, "$.drugstoreNumber")).isEqualTo(900_000_001);
        assertThat(JsonPath.<Double>read(corpo, "$.drugstoreCardBalance")).isEqualTo(999999.0);
    }
}
