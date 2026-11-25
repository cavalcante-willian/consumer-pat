package br.com.alelo.consumer.consumerpat.api.comum.requisicao;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * O identificador de uma requisição, que liga a resposta, o erro e todas as linhas do log dela.
 *
 * <p>
 * O cliente pode mandá-lo em {@code X-Request-Id}, para correlacionar com o sistema dele, mas só é
 * aceito se tiver o formato de um UUID. Texto livre do cliente num log é injeção de log, e por isso
 * qualquer outro valor é descartado e um id novo é gerado.
 */
public final class IdDeRequisicao {

    /** O cabeçalho em que o id chega e volta, em toda resposta. */
    public static final String CABECALHO = "X-Request-Id";

    /** O nome do id no {@code MDC}, que o padrão do log lê por {@code %X{idRequisicao}}. */
    public static final String CHAVE_NO_MDC = "idRequisicao";

    private static final Pattern FORMATO_DE_UUID = Pattern
            .compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private IdDeRequisicao() {
    }

    /**
     * Decide o id da requisição.
     *
     * @param cabecalho o valor de {@code X-Request-Id}, ou nulo se o cliente não mandou
     * @return o mesmo valor, se for um UUID, ou um UUID novo
     */
    public static String aceitarOuGerar(String cabecalho) {
        return Optional.ofNullable(cabecalho).filter(id -> FORMATO_DE_UUID.matcher(id).matches())
                .orElseGet(() -> UUID.randomUUID().toString());
    }
}
