package br.com.alelo.consumer.consumerpat.api.comum.campos;

import com.fasterxml.jackson.databind.JsonMappingException;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Descobre qual campo do JSON o Jackson não conseguiu ler.
 *
 * <p>
 * O nome é o do JSON, e não o da classe, e sai com ponto quando o campo está dentro de outro. Só o
 * caminho do campo é usado: o texto da exceção cita nome de classe do projeto, e nada disso vai ao
 * cliente.
 */
public final class NomeDoCampoDoJackson {

    /** O nome usado quando o erro não é de um campo, e sim do corpo todo. */
    public static final String CORPO = "corpo";

    private NomeDoCampoDoJackson() {
    }

    /**
     * Devolve o caminho do campo.
     *
     * @param excecao a exceção do Jackson
     * @return o caminho do campo, ou {@value #CORPO} se não há campo
     */
    public static String de(JsonMappingException excecao) {
        return Optional
                .of(excecao.getPath().stream().map(JsonMappingException.Reference::getFieldName)
                        .filter(Objects::nonNull).collect(Collectors.joining(".")))
                .filter(caminho -> !caminho.isEmpty()).orElse(CORPO);
    }
}
