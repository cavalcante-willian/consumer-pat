package br.com.alelo.consumer.consumerpat.api.comum.campos;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import java.util.List;
import java.util.Map;

/**
 * O registro das causas do Jackson que a API sabe traduzir, por tipo de exceção, e o que cada uma
 * diz ao cliente.
 *
 * <p>
 * É só o registro: quem o consulta é {@link ErrosDeCampoDoCorpo}. Uma causa nova é uma linha nova
 * aqui, e o algoritmo não muda. A ordem das entradas não importa: a consulta sobe pela hierarquia a
 * partir da classe real da exceção, de modo que o tipo mais específico vence sozinho, e o campo
 * desconhecido, que é uma especialização do formato errado, nunca é engolido por ele.
 */
public final class TradutoresDoCorpo {

    /** O tradutor de cada tipo de exceção do Jackson que a API reconhece. */
    public static final Map<Class<? extends Throwable>, TradutorDeCausa> POR_CLASSE = Map.of(
            UnrecognizedPropertyException.class,
            causa -> Map.of(NomeDoCampoDoJackson.de((JsonMappingException) causa),
                    List.of("não é aceito nesta operação")),
            InvalidFormatException.class,
            causa -> Map.of(NomeDoCampoDoJackson.de((JsonMappingException) causa),
                    List.of("não tem o formato esperado")),
            MismatchedInputException.class,
            causa -> Map.of(NomeDoCampoDoJackson.de((JsonMappingException) causa),
                    List.of("não tem o formato esperado")),
            JsonParseException.class,
            causa -> Map.of(NomeDoCampoDoJackson.CORPO, List.of("não é um JSON válido")));

    private TradutoresDoCorpo() {
    }
}
