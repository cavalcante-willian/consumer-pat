package br.com.alelo.consumer.consumerpat.api.comum.campos;

import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Traduz a falha de leitura do corpo nos campos com erro da resposta.
 *
 * <p>
 * O texto do Jackson cita nomes de classe do projeto e a posição no corpo, e nada disso vai ao
 * cliente. Percorre a cadeia de causas da exceção, da mais externa para a mais interna, e para cada
 * uma sobe pela hierarquia de classes, da real para as mais gerais, até achar um tradutor no
 * {@link TradutoresDoCorpo#POR_CLASSE registro}. Quando nenhuma é reconhecida, o corpo é dito
 * ilegível, ou obrigatório se nem veio, e a mensagem do framework também não sai.
 */
public final class ErrosDeCampoDoCorpo {

    /**
     * O início da mensagem que o Spring põe na falha de leitura quando o corpo nem veio. É texto do
     * framework, em inglês e fixo, e não da API: serve só para reconhecer o caso, porque essa falha
     * não tem exceção do Jackson por baixo. O que o cliente lê é a tradução em português, e um
     * teste provoca o corpo ausente de verdade, de modo que uma troca de redação do Spring numa
     * atualização quebra o teste, e não passa em silêncio.
     */
    private static final String MARCA_DO_CORPO_AUSENTE = "Required request body is missing";

    private ErrosDeCampoDoCorpo() {
    }

    /**
     * Traduz a falha de leitura.
     *
     * @param excecao a exceção do framework
     * @return o campo com erro e a sua mensagem
     */
    public static Map<String, List<String>> de(HttpMessageNotReadableException excecao) {
        return Stream.iterate(excecao.getCause(), Objects::nonNull, Throwable::getCause)
                .flatMap(causa -> Stream
                        .<Class<?>>iterate(causa.getClass(), Objects::nonNull, Class::getSuperclass)
                        .flatMap(tipo -> Optional.ofNullable(TradutoresDoCorpo.POR_CLASSE.get(tipo))
                                .stream())
                        .findFirst().map(tradutor -> tradutor.traduzir(causa)).stream())
                .findFirst()
                .orElseGet(() -> Map.of(NomeDoCampoDoJackson.CORPO,
                        List.of(Optional.ofNullable(excecao.getMessage())
                                .filter(texto -> texto.startsWith(MARCA_DO_CORPO_AUSENTE))
                                .map(texto -> "é obrigatório").orElse("não pôde ser lido"))));
    }
}
