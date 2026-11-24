package br.com.alelo.consumer.consumerpat.api.comum.resposta;

import br.com.alelo.consumer.consumerpat.api.comum.problema.MensagemProblema;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Transforma um corpo de erro na resposta HTTP, com o status dele e o tipo de mídia de problem
 * details.
 */
public final class RespostaDeProblema {

    /** O tipo de mídia do corpo de erro. */
    public static final MediaType TIPO_DE_MIDIA = MediaType.valueOf("application/problem+json");

    private RespostaDeProblema() {
    }

    /**
     * Monta a resposta.
     *
     * @param problema o corpo de erro
     * @return a resposta, com o status do próprio corpo
     */
    public static ResponseEntity<Object> de(MensagemProblema problema) {
        return ResponseEntity.status(problema.status()).contentType(TIPO_DE_MIDIA).body(problema);
    }
}
