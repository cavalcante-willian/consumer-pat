package br.com.alelo.consumer.consumerpat.api.cartao.compra;

import br.com.alelo.consumer.consumerpat.api.comum.documentacao.TagsDaApi;
import br.com.alelo.consumer.consumerpat.api.comum.resposta.RespostaDeResultado;
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.compra.RegistrarCompraCaso;
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
 * O endpoint de compra: {@code POST /api/v1/compras}.
 *
 * <p>
 * É fino: decodifica a mensagem, chama o caso de uso e devolve o comprovante. Não escolhe status de
 * erro nem monta corpo de erro, e o número do cartão e o valor vão no corpo, nunca na URL, onde
 * acabariam em log de acesso. A compra altera estado, então é um {@code POST}: no legado era um
 * {@code GET}, e qualquer pré-busca de navegador geraria uma compra. Não é idempotente, e repetir a
 * chamada às cegas debita duas vezes. Responde 201 sem {@code Location}, porque o extrato criado
 * ainda não tem rota de leitura.
 */
@Tag(name = TagsDaApi.CARTOES)
@RestController
@RequestMapping("/api/v1/compras")
public class CompraController {

    private final RegistrarCompraCaso caso;

    CompraController(RegistrarCompraCaso caso) {
        this.caso = caso;
    }

    /**
     * Registra uma compra.
     *
     * @param mensagem   o corpo da compra
     * @param requisicao a requisição, para o caminho do corpo de erro
     * @return 201 com o comprovante, ou o erro padronizado
     */
    @Operation(summary = "Registra uma compra", description = """
            Debita o cartão do tipo do estabelecimento (1 alimentação, 2 farmácia, 3 combustível), \
            com o preço do tipo: alimentação com 10% de desconto, combustível com acréscimo de \
            35% e farmácia pelo valor integral. O extrato guarda o valor cobrado. Não é \
            idempotente: repetir a chamada debita de novo.""")
    @ApiResponse(responseCode = "201", description = "Compra registrada. O corpo é o comprovante.",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MensagemComprovanteCompra.class)))
    @ApiResponse(responseCode = "400",
            description = "Entrada inválida: campo ausente, formato errado, tipo de "
                    + "estabelecimento desconhecido ou valor que não é positivo.")
    @ApiResponse(responseCode = "404",
            description = "Não existe cartão desse tipo com esse número.")
    @ApiResponse(responseCode = "409",
            description = "O cartão está em uso por outra requisição, ou o saldo mudou no meio. "
                    + "Tente de novo.")
    @ApiResponse(responseCode = "415", description = "O corpo não é application/json.")
    @ApiResponse(responseCode = "422", description = "O saldo não cobre a compra.")
    @PostMapping
    public ResponseEntity<Object> registrar(@Valid @RequestBody MensagemRegistrarCompra mensagem,
            HttpServletRequest requisicao) {
        return RespostaDeResultado.responder(
                CompraDaMensagem.converter(mensagem).encadear(caso::registrar),
                ComprovanteDaCompraEmMensagem::montar, HttpStatus.CREATED,
                requisicao.getRequestURI());
    }
}
