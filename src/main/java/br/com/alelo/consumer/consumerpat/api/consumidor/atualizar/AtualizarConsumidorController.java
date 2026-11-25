package br.com.alelo.consumer.consumerpat.api.consumidor.atualizar;

import br.com.alelo.consumer.consumerpat.api.comum.resposta.RespostaDeResultado;
import br.com.alelo.consumer.consumerpat.api.consumidor.ConsumidorEmMensagem;
// editorconfig-checker-disable-next-line
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.atualizar.AtualizarConsumidorCaso;
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
    @PutMapping("/{id}")
    public ResponseEntity<Object> atualizar(@PathVariable long id,
            @Valid @RequestBody MensagemAtualizarConsumidor mensagem,
            HttpServletRequest requisicao) {
        return RespostaDeResultado.responder(
                AtualizacaoDaMensagem.converter(id, mensagem).encadear(caso::atualizar),
                ConsumidorEmMensagem::montar, HttpStatus.OK, requisicao.getRequestURI());
    }
}
