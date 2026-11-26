package br.com.alelo.consumer.consumerpat.api.saude;

import br.com.alelo.consumer.consumerpat.api.comum.documentacao.TagsDaApi;
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.saude.ConsultarSaudeCaso;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = TagsDaApi.SAUDE)
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
    @Operation(summary = "Consulta a saúde da aplicação", description = """
            Verifica as dependências críticas, hoje o banco. É público e o corpo não leva nada \
            interno, só o estado geral e o de cada dependência.""")
    @ApiResponse(responseCode = "200", description = "Tudo responde: o estado é `SAUDAVEL`.",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MensagemSaude.class)))
    @ApiResponse(responseCode = "503",
            description = "Alguma dependência crítica não responde: o estado é `DEGRADADO`.",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MensagemSaude.class)))
    @GetMapping
    public ResponseEntity<MensagemSaude> consultar() {
        var estado = caso.consultar();
        return ResponseEntity.status(StatusDaSaude.de(estado.situacao()))
                .body(SaudeEmMensagem.montar(estado));
    }
}
