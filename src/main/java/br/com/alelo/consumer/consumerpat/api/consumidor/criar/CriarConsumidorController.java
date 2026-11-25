package br.com.alelo.consumer.consumerpat.api.consumidor.criar;

import br.com.alelo.consumer.consumerpat.api.comum.resposta.RespostaDeResultadoCriado;
import br.com.alelo.consumer.consumerpat.api.consumidor.ConsumidorEmMensagem;
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.criar.CriarConsumidorCaso;
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
