package br.com.alelo.consumer.consumerpat.api.comum.tratamento;

import br.com.alelo.consumer.consumerpat.api.comum.campos.ErrosDeCampoDeValidacao;
import br.com.alelo.consumer.consumerpat.api.comum.problema.ProblemaDeBorda;
import br.com.alelo.consumer.consumerpat.api.comum.problema.ProblemaDeBordaEmMensagem;
import br.com.alelo.consumer.consumerpat.api.comum.resposta.RespostaDeProblema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * Papel do tratador que traduz a falha da validação do corpo: o 400 com todos os campos de uma vez.
 */
public interface TraducaoDeEntradaInvalida {

    /** O detalhe, o mesmo de toda falha de campo. */
    String DETALHE_DE_CAMPOS = "Um ou mais campos precisam de correção.";

    /**
     * Traduz a falha da validação.
     *
     * @param excecao    a falha da validação do corpo
     * @param requisicao a requisição que falhou
     * @return o 400 com os campos com erro
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    default ResponseEntity<Object> traduzirEntradaInvalida(MethodArgumentNotValidException excecao,
            HttpServletRequest requisicao) {
        return RespostaDeProblema.de(ProblemaDeBordaEmMensagem.montar(
                ProblemaDeBorda.ENTRADA_INVALIDA, DETALHE_DE_CAMPOS, requisicao.getRequestURI(),
                ErrosDeCampoDeValidacao.de(excecao.getBindingResult())));
    }
}
