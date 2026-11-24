package br.com.alelo.consumer.consumerpat.api.cartao.credito;

import br.com.alelo.consumer.consumerpat.api.comum.resposta.RespostaDeResultado;
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.CreditarCartaoCaso;
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
    @PostMapping
    public ResponseEntity<Object> creditar(@Valid @RequestBody MensagemCreditarCartao mensagem,
            HttpServletRequest requisicao) {
        return RespostaDeResultado.responder(
                CreditoDaMensagem.converter(mensagem).encadear(caso::creditar),
                ComprovanteDoCreditoEmMensagem::montar, HttpStatus.CREATED,
                requisicao.getRequestURI());
    }
}
