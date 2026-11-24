package br.com.alelo.consumer.consumerpat.api.comum.campos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;

import java.nio.charset.StandardCharsets;

/**
 * Provoca as falhas de leitura de verdade, com o Jackson de verdade, para os testes de tradução não
 * dependerem de uma imitação da exceção.
 */
final class FalhasDeLeituraDeTeste {

    /** Um corpo com um campo numérico e um de texto. */
    record Exemplo(Integer numero, String texto) {
    }

    /** Um corpo que tem outro dentro, para o campo ter caminho com ponto. */
    record Aninhado(Exemplo exemplo) {
    }

    private FalhasDeLeituraDeTeste() {
    }

    /**
     * Lê o JSON como {@link Exemplo} e devolve a exceção do Jackson.
     *
     * @param json o corpo que deve falhar
     * @return a exceção que o Jackson lança
     */
    static JsonProcessingException excecaoDoExemplo(String json) {
        return excecao(json, Exemplo.class);
    }

    /**
     * Lê o JSON como {@link Aninhado} e devolve a exceção do Jackson.
     *
     * @param json o corpo que deve falhar
     * @return a exceção que o Jackson lança
     */
    static JsonMappingException excecaoDoAninhado(String json) {
        return (JsonMappingException) excecao(json, Aninhado.class);
    }

    /**
     * Embrulha a exceção do Jackson na do Spring, como o framework faz.
     *
     * @param json o corpo que deve falhar
     * @return a exceção de leitura com o Jackson por baixo
     */
    static HttpMessageNotReadableException leituraDoExemplo(String json) {
        return new HttpMessageNotReadableException("texto do framework", excecaoDoExemplo(json),
                new MockHttpInputMessage(json.getBytes(StandardCharsets.UTF_8)));
    }

    private static JsonProcessingException excecao(String json, Class<?> tipo) {
        try {
            new ObjectMapper().readValue(json, tipo);
            throw new AssertionError("o corpo devia falhar ao ser lido: " + json);
        } catch (JsonProcessingException e) {
            return e;
        }
    }
}
