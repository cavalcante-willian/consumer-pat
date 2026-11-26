package br.com.alelo.consumer.consumerpat.fabricas;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonDeTesteTeste {

    @Test
    void deve_deixar_o_texto_comum_como_esta_quando_escapar() {
        // Agir e Verificar
        assertThat(JsonDeTeste.escapar("Consumidor de teste")).isEqualTo("Consumidor de teste");
    }

    @Test
    void deve_escapar_aspas_barras_e_caracteres_de_controle_quando_escapar() {
        // Agir
        var escapado = JsonDeTeste.escapar("a\"b\\c\n\u0001");

        // Verificar
        assertThat(escapado).isEqualTo("a\\\"b\\\\c\\n\\u0001");
    }

    @Test
    void deve_manter_o_json_valido_e_o_valor_igual_ao_original_quando_o_texto_e_hostil() {
        // Preparar
        var hostil = "Robert'); DROP TABLE x;--\"} , {\"hacker\":1";

        // Agir
        var json = "{\"nome\":\"" + JsonDeTeste.escapar(hostil) + "\"}";

        // Verificar: o texto não escapa da string, e não vira campo nenhum
        assertThat(JsonPath.<String>read(json, "$.nome")).isEqualTo(hostil);
        assertThatThrownBy(() -> JsonPath.<Object>read(json, "$.hacker"))
                .isInstanceOf(PathNotFoundException.class);
    }
}
