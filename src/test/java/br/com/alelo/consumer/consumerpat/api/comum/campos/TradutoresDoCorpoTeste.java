package br.com.alelo.consumer.consumerpat.api.comum.campos;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TradutoresDoCorpoTeste {

    @Test
    void deve_registrar_as_quatro_causas_que_a_api_sabe_traduzir_quando_consultar_o_registro() {
        // Verificar
        assertThat(TradutoresDoCorpo.POR_CLASSE).containsOnlyKeys(
                UnrecognizedPropertyException.class, InvalidFormatException.class,
                MismatchedInputException.class, JsonParseException.class);
    }

    @Test
    void deve_manter_o_campo_desconhecido_como_especializacao_do_formato_errado_quando_consultar() {
        // Verificar: é por isso que a consulta sobe pela hierarquia, e a ordem não importa
        assertThat(MismatchedInputException.class)
                .isAssignableFrom(UnrecognizedPropertyException.class);
        assertThat(MismatchedInputException.class).isAssignableFrom(InvalidFormatException.class);
    }

    @Test
    void deve_traduzir_a_causa_real_do_jackson_quando_aplicar_o_tradutor_do_campo_desconhecido() {
        // Preparar
        var causa = FalhasDeLeituraDeTeste.excecaoDoExemplo("{\"saldo\":1}");
        var tradutor = TradutoresDoCorpo.POR_CLASSE.get(UnrecognizedPropertyException.class);

        // Agir
        var erros = tradutor.traduzir(causa);

        // Verificar
        assertThat(erros).containsOnlyKeys("saldo");
    }

    @Test
    void deve_traduzir_a_causa_real_do_jackson_quando_aplicar_o_tradutor_do_json_malformado() {
        // Preparar
        var causa = FalhasDeLeituraDeTeste.excecaoDoExemplo("{\"numero\":");
        var tradutor = TradutoresDoCorpo.POR_CLASSE.get(JsonParseException.class);

        // Agir
        var erros = tradutor.traduzir(causa);

        // Verificar
        assertThat(erros).containsOnlyKeys("corpo");
    }
}
