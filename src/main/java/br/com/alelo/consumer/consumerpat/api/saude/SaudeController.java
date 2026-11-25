package br.com.alelo.consumer.consumerpat.api.saude;

import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.saude.ConsultarSaudeCaso;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code GET /api/v1/saude}: o health check, por dependência.
 *
 * <p>
 * Para o balanceador e o orquestrador: {@code 200} se tudo responde e {@code 503} se uma
 * dependência crítica não responde. É público e sem autenticação, e por isso o corpo não leva nada
 * interno. A resposta nunca vai para cache, porque um health velho diz o que não é mais verdade, e
 * o {@code Cache-Control: no-store} vem do filtro dos cabeçalhos de segurança, como o de toda
 * resposta da API.
 */
@RestController
@RequestMapping("/api/v1/saude")
public class SaudeController {

    private final ConsultarSaudeCaso caso;

    SaudeController(ConsultarSaudeCaso caso) {
        this.caso = caso;
    }

    /**
     * Consulta a saúde.
     *
     * @return {@code 200} com {@code SAUDAVEL}, ou {@code 503} com {@code DEGRADADO}
     */
    @GetMapping
    public ResponseEntity<MensagemSaude> consultar() {
        var estado = caso.consultar();
        return ResponseEntity.status(StatusDaSaude.de(estado.situacao()))
                .body(SaudeEmMensagem.montar(estado));
    }
}
