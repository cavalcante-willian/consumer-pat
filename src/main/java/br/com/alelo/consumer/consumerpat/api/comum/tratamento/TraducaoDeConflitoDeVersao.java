package br.com.alelo.consumer.consumerpat.api.comum.tratamento;

import br.com.alelo.consumer.consumerpat.api.comum.problema.ProblemaDeBorda;
import br.com.alelo.consumer.consumerpat.api.comum.problema.ProblemaDeBordaEmMensagem;
import br.com.alelo.consumer.consumerpat.api.comum.resposta.RespostaDeProblema;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * Papel do tratador que traduz o conflito de versão de linha: o 409.
 *
 * <p>
 * É a rede de segurança da coluna de versão. Se a trava do saldo for removida sem querer, duas
 * gravações simultâneas deixam de se enfileirar e a segunda falha aqui, em vez de apagar a primeira
 * em silêncio.
 */
public interface TraducaoDeConflitoDeVersao {

    /**
     * Traduz o conflito de versão.
     *
     * @param excecao    a gravação que perdeu a corrida
     * @param requisicao a requisição que falhou
     * @return o 409
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    default ResponseEntity<Object> traduzirConflitoDeVersao(
            ObjectOptimisticLockingFailureException excecao, HttpServletRequest requisicao) {
        return RespostaDeProblema.de(ProblemaDeBordaEmMensagem.montar(
                ProblemaDeBorda.CONFLITO_DE_CONCORRENCIA,
                "O dado foi alterado por outra requisição. Confira antes de tentar de novo.",
                requisicao.getRequestURI(), Map.of()));
    }
}
