package br.com.alelo.consumer.consumerpat.api.comum.tratamento;

import br.com.alelo.consumer.consumerpat.api.comum.problema.ProblemaDeBorda;
import br.com.alelo.consumer.consumerpat.api.comum.problema.ProblemaDeBordaEmMensagem;
import br.com.alelo.consumer.consumerpat.api.comum.resposta.RespostaDeProblema;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

import static br.com.alelo.consumer.consumerpat.dominio.log.CampoDeLog.campo;

/**
 * Traduz uma restrição do banco violada no meio de uma gravação em {@code 409}.
 *
 * <p>
 * O adaptador confere a unicidade antes de gravar, e devolve o erro certo, de documento ou de
 * número de cartão. O que chega aqui é a corrida: dois cadastros simultâneos que passaram na
 * conferência, e o banco recusou o segundo. O cliente não precisa saber qual restrição foi: a
 * mensagem do banco, que cita tabela, coluna e valor, nunca vai ao corpo nem ao log: o log leva o
 * nome da restrição, em {@code conflito.detectado}, e nunca o valor duplicado.
 */
public interface TraducaoDeIntegridadeViolada extends ComSaidaDeLog {

    /**
     * Traduz a violação.
     *
     * @param excecao    a violação de integridade
     * @param requisicao a requisição que a provocou
     * @return o problema {@code 409}
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    default ResponseEntity<Object> traduzirIntegridadeViolada(
            DataIntegrityViolationException excecao, HttpServletRequest requisicao) {
        saida().aviso("conflito.detectado", campo("metodo", requisicao.getMethod()),
                campo("caminho", requisicao.getRequestURI()),
                campo("restricao", NomeDaRestricao.de(excecao)));
        return RespostaDeProblema
                .de(ProblemaDeBordaEmMensagem
                        .montar(ProblemaDeBorda.CONFLITO_DE_CONCORRENCIA,
                                "Outra requisição gravou um dado igual ao mesmo tempo. "
                                        + "Releia e tente de novo.",
                                requisicao.getRequestURI(), Map.of()));
    }
}
