package br.com.alelo.consumer.consumerpat.api.consumidor.criar;

import br.com.alelo.consumer.consumerpat.api.comum.documentacao.TagsDaApi;
import br.com.alelo.consumer.consumerpat.api.comum.resposta.RespostaDeResultadoCriado;
import br.com.alelo.consumer.consumerpat.api.consumidor.ConsumidorEmMensagem;
import br.com.alelo.consumer.consumerpat.api.consumidor.MensagemConsumidor;
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.criar.CriarConsumidorCaso;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * {@code POST /api/v1/consumidores}: cadastra um consumidor com os cartões que ele traz.
 *
 * <p>
 * Fino: converte o corpo, chama o caso de uso e entrega o resultado à resposta. Responde
 * {@code 201} com {@code Location} para a leitura, porque o consumidor tem rota de leitura. É
 * {@code POST} e leva o documento no corpo, que não pode ir na URL.
 */
@Tag(name = TagsDaApi.CONSUMIDORES)
@RestController
@RequestMapping("/api/v1/consumidores")
public class CriarConsumidorController {

    private final CriarConsumidorCaso caso;

    CriarConsumidorController(CriarConsumidorCaso caso) {
        this.caso = caso;
    }

    /**
     * Cadastra o consumidor.
     *
     * @param mensagem   o corpo do cadastro
     * @param requisicao a requisição, para o {@code Location} e o caminho do problema
     * @return {@code 201} com o consumidor, ou o problema
     */
    @Operation(summary = "Cadastra um consumidor", description = """
            Cria o consumidor e os cartões pedidos, cada um com saldo zero. O documento e o número \
            de cada cartão são únicos, e o documento aceita pontuação. O corpo devolvido traz a \
            `versao`, que a atualização exige.""")
    @ApiResponse(responseCode = "201", description = "Consumidor cadastrado.",
            headers = @Header(name = "Location", description = "A rota do consumidor criado."),
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MensagemConsumidor.class)))
    @ApiResponse(responseCode = "400",
            description = "Entrada inválida: campo ausente ou com formato errado.")
    @ApiResponse(responseCode = "409",
            description = "O documento ou o número de algum cartão já pertence a outro "
                    + "consumidor.")
    @ApiResponse(responseCode = "415", description = "O corpo não é application/json.")
    @PostMapping
    public ResponseEntity<Object> criar(@Valid @RequestBody MensagemCriarConsumidor mensagem,
            HttpServletRequest requisicao) {
        return RespostaDeResultadoCriado.responder(
                NovoConsumidorDaMensagem.converter(mensagem).encadear(caso::criar),
                ConsumidorEmMensagem::montar,
                consumidor -> LocalizacaoDoConsumidor.de(requisicao.getRequestURI(), consumidor),
                requisicao.getRequestURI());
    }
}
