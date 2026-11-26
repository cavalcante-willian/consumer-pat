package br.com.alelo.consumer.consumerpat.api.consumidor.atualizar;

import br.com.alelo.consumer.consumerpat.api.comum.documentacao.TagsDaApi;
import br.com.alelo.consumer.consumerpat.api.comum.resposta.RespostaDeResultado;
import br.com.alelo.consumer.consumerpat.api.consumidor.ConsumidorEmMensagem;
import br.com.alelo.consumer.consumerpat.api.consumidor.MensagemConsumidor;
// editorconfig-checker-disable-next-line
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.atualizar.AtualizarConsumidorCaso;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * {@code PUT /api/v1/consumidores/{id}}: atualiza o cadastro de um consumidor, e só o cadastro.
 *
 * <p>
 * Fino: converte o id e o corpo, chama o caso de uso e entrega o resultado à resposta. Responde
 * {@code 200} com o consumidor como ficou, e a versão nova.
 */
@Tag(name = TagsDaApi.CONSUMIDORES)
@RestController
@RequestMapping("/api/v1/consumidores")
public class AtualizarConsumidorController {

    private final AtualizarConsumidorCaso caso;

    AtualizarConsumidorController(AtualizarConsumidorCaso caso) {
        this.caso = caso;
    }

    /**
     * Atualiza o cadastro.
     *
     * @param id         o id da rota
     * @param mensagem   o corpo da atualização
     * @param requisicao a requisição, para o caminho do problema
     * @return {@code 200} com o consumidor, ou o problema
     */
    @Operation(summary = "Atualiza os dados cadastrais", description = """
            Troca os dados cadastrais do consumidor. O corpo traz a `versao` lida antes: se outra \
            atualização passou na frente, a resposta é 409. Não altera os cartões nem o saldo, e \
            um campo de saldo no corpo é recusado.""")
    @ApiResponse(responseCode = "200", description = "Consumidor atualizado, com a versão nova.",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MensagemConsumidor.class)))
    @ApiResponse(responseCode = "400",
            description = "Entrada inválida: id que não é positivo, campo ausente, com formato "
                    + "errado ou desconhecido, como o saldo.")
    @ApiResponse(responseCode = "404", description = "Não existe consumidor com esse id.")
    @ApiResponse(responseCode = "409",
            description = "A versão enviada não é mais a atual, ou o documento já pertence a "
                    + "outro consumidor. Leia o consumidor de novo.")
    @ApiResponse(responseCode = "415", description = "O corpo não é application/json.")
    @PutMapping("/{id}")
    public ResponseEntity<Object> atualizar(@PathVariable long id,
            @Valid @RequestBody MensagemAtualizarConsumidor mensagem,
            HttpServletRequest requisicao) {
        return RespostaDeResultado.responder(
                AtualizacaoDaMensagem.converter(id, mensagem).encadear(caso::atualizar),
                ConsumidorEmMensagem::montar, HttpStatus.OK, requisicao.getRequestURI());
    }
}
