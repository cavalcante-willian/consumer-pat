package br.com.alelo.consumer.consumerpat.api.consumidor.obter;

import br.com.alelo.consumer.consumerpat.api.comum.documentacao.TagsDaApi;
import br.com.alelo.consumer.consumerpat.api.comum.resposta.RespostaDeResultado;
import br.com.alelo.consumer.consumerpat.api.consumidor.ConsumidorEmMensagem;
import br.com.alelo.consumer.consumerpat.api.consumidor.MensagemConsumidor;
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.obter.ObterConsumidorCaso;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorIdDeNumero;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * {@code GET /api/v1/consumidores/{id}}: lê um consumidor, com os cartões e a versão.
 *
 * <p>
 * É a leitura que o cliente faz antes de atualizar, e o destino do {@code Location} do cadastro. O
 * documento, o celular e os cartões saem mascarados.
 */
@Tag(name = TagsDaApi.CONSUMIDORES)
@RestController
@RequestMapping("/api/v1/consumidores")
public class ObterConsumidorController {

    private final ObterConsumidorCaso caso;

    ObterConsumidorController(ObterConsumidorCaso caso) {
        this.caso = caso;
    }

    /**
     * Lê o consumidor.
     *
     * @param id         o id da rota
     * @param requisicao a requisição, para o caminho do problema
     * @return {@code 200} com o consumidor, ou o problema
     */
    @Operation(summary = "Consulta um consumidor", description = """
            Devolve o consumidor com os seus cartões. O documento, os telefones e o número do \
            cartão vêm mascarados, e o saldo, em texto decimal.""")
    @ApiResponse(responseCode = "200", description = "O consumidor, com a `versao` para atualizar.",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MensagemConsumidor.class)))
    @ApiResponse(responseCode = "400", description = "O id não é um número positivo.")
    @ApiResponse(responseCode = "404", description = "Não existe consumidor com esse id.")
    @GetMapping("/{id}")
    public ResponseEntity<Object> obter(@PathVariable long id, HttpServletRequest requisicao) {
        return RespostaDeResultado.responder(
                ConsumidorIdDeNumero.converter(id).encadear(caso::obter),
                ConsumidorEmMensagem::montar, HttpStatus.OK, requisicao.getRequestURI());
    }
}
