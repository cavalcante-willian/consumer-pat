package br.com.alelo.consumer.consumerpat.api.saude;

import br.com.alelo.consumer.consumerpat.dominio.saude.SituacaoDeSaude;
import org.springframework.http.HttpStatus;

import java.util.EnumMap;
import java.util.Map;

/**
 * O status HTTP de cada situação de saúde.
 *
 * <p>
 * É o único endpoint do projeto que responde {@code 503}: dependência crítica indisponível. O
 * domínio não conhece HTTP, e a tradução mora aqui, na borda.
 */
public final class StatusDaSaude {

    private static final Map<SituacaoDeSaude, HttpStatus> STATUS = new EnumMap<>(
            Map.of(SituacaoDeSaude.SAUDAVEL, HttpStatus.OK, SituacaoDeSaude.DEGRADADO,
                    HttpStatus.SERVICE_UNAVAILABLE));

    private StatusDaSaude() {
    }

    /**
     * Escolhe o status.
     *
     * @param situacao a situação geral
     * @return {@code 200} se saudável, ou {@code 503}
     */
    public static HttpStatus de(SituacaoDeSaude situacao) {
        return STATUS.get(situacao);
    }
}
