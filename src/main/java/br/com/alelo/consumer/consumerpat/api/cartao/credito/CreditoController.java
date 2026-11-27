package br.com.alelo.consumer.consumerpat.api.cartao.credito;

import br.com.alelo.consumer.consumerpat.api.comum.documentacao.TagsDaApi;
import br.com.alelo.consumer.consumerpat.api.comum.resposta.RespostaDeResultado;
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.credito.CreditarCartaoCaso;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * O endpoint de crédito: {@code POST /api/v1/creditos}.
 *
 * <p>
 * É fino, como o de compra: decodifica a mensagem, chama o caso de uso e devolve o comprovante. O
 * número do cartão e o valor vão no corpo, nunca na URL. O crédito altera estado, então é um
 * {@code POST}, e não é idempotente: repetir a chamada às cegas credita duas vezes. Responde 201
 * sem {@code Location}, porque ainda não há rota de leitura para o que foi criado.
 */
@Tag(name = TagsDaApi.CARTOES)
@RestController
@RequestMapping("/api/v1/creditos")
public class CreditoController {

    private final CreditarCartaoCaso caso;

    CreditoController(CreditarCartaoCaso caso) {
        this.caso = caso;
    }

    /**
     * Credita um valor no cartão.
     *
     * @param mensagem   o corpo do crédito
     * @param requisicao a requisição, para o caminho do corpo de erro
     * @return 201 com o comprovante, ou o erro padronizado
     */
    @Operation(summary = "Credita um valor num cartão", description = """
            Soma o valor ao saldo do cartão do tipo informado (1 alimentação, 2 farmácia, \
            3 combustível). O valor é positivo, com até duas casas, e o crédito não gera linha \
            no extrato.""")
    @ApiResponse(responseCode = "201",
            description = "Crédito registrado. O corpo traz o saldo novo.",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MensagemComprovanteCredito.class)))
    @ApiResponse(responseCode = "400",
            description = "Entrada inválida: campo ausente, formato errado, tipo de "
                    + "estabelecimento desconhecido, valor que não é positivo ou com mais de "
                    + "duas casas.")
    @ApiResponse(responseCode = "404",
            description = "Não existe cartão desse tipo com esse número.")
    @ApiResponse(responseCode = "409",
            description = "O cartão está em uso por outra requisição, ou o saldo mudou no meio. "
                    + "Tente de novo.")
    @ApiResponse(responseCode = "415", description = "O corpo não é application/json.")
    @PostMapping
    public ResponseEntity<Object> creditar(@Valid @RequestBody MensagemCreditarCartao mensagem,
            HttpServletRequest requisicao) {
        return RespostaDeResultado.responder(
                CreditoDaMensagem.converter(mensagem).encadear(caso::creditar),
                ComprovanteDoCreditoEmMensagem::montar, HttpStatus.CREATED,
                requisicao.getRequestURI());
    }
}
