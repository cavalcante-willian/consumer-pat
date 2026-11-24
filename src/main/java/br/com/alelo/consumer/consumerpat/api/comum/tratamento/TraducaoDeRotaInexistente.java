package br.com.alelo.consumer.consumerpat.api.comum.tratamento;

import br.com.alelo.consumer.consumerpat.api.comum.problema.ProblemaDeBorda;
import br.com.alelo.consumer.consumerpat.api.comum.problema.ProblemaDeBordaEmMensagem;
import br.com.alelo.consumer.consumerpat.api.comum.resposta.RespostaDeProblema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * Papel do tratador que traduz a rota inexistente: o 404 padronizado, e não a página de erro do
 * framework.
 */
public interface TraducaoDeRotaInexistente {

    /**
     * Traduz a rota inexistente.
     *
     * @param excecao    a rota que ninguém atende
     * @param requisicao a requisição que falhou
     * @return o 404
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    default ResponseEntity<Object> traduzirRotaInexistente(NoHandlerFoundException excecao,
            HttpServletRequest requisicao) {
        return RespostaDeProblema.de(ProblemaDeBordaEmMensagem.montar(
                ProblemaDeBorda.ROTA_NAO_ENCONTRADA, "Não existe rota para esta requisição.",
                requisicao.getRequestURI(), Map.of()));
    }
}
