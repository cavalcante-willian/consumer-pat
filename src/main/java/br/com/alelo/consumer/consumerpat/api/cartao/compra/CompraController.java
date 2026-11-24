package br.com.alelo.consumer.consumerpat.api.cartao.compra;

import br.com.alelo.consumer.consumerpat.api.comum.resposta.RespostaDeResultado;
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao.RegistrarCompraCaso;
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
    @PostMapping
    public ResponseEntity<Object> registrar(@Valid @RequestBody MensagemRegistrarCompra mensagem,
            HttpServletRequest requisicao) {
        return RespostaDeResultado.responder(
                CompraDaMensagem.converter(mensagem).encadear(caso::registrar),
                ComprovanteDaCompraEmMensagem::montar, HttpStatus.CREATED,
                requisicao.getRequestURI());
    }
}
