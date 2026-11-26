package br.com.alelo.consumer.consumerpat.api.comum.tratamento;

import br.com.alelo.consumer.consumerpat.api.comum.problema.ProblemaDeBorda;
import br.com.alelo.consumer.consumerpat.api.comum.problema.ProblemaDeBordaEmMensagem;
import br.com.alelo.consumer.consumerpat.api.comum.resposta.RespostaDeProblema;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

import static br.com.alelo.consumer.consumerpat.dominio.log.CampoDeLog.campo;

/**
 * Papel do tratador que traduz a espera por trava que estourou: o 409, que diz para tentar de novo.
 *
 * <p>
 * O cartão estava em uso por outra requisição e a espera passou do teto. É um conflito que o
 * cliente não causou, e por isso não é 500; mas a compra não é idempotente, então o detalhe orienta
 * a conferir antes de repetir, e não a repetir às cegas. É registrado como {@code lock.timeout},
 * com a operação.
 */
public interface TraducaoDeTravaEsgotada extends ComSaidaDeLog {

    /**
     * Traduz a trava esgotada.
     *
     * @param excecao    a espera que estourou
     * @param requisicao a requisição que falhou
     * @return o 409
     */
    @ExceptionHandler(PessimisticLockingFailureException.class)
    default ResponseEntity<Object> traduzirTravaEsgotada(PessimisticLockingFailureException excecao,
            HttpServletRequest requisicao) {
        saida().aviso("lock.timeout", campo("metodo", requisicao.getMethod()),
                campo("caminho", requisicao.getRequestURI()));
        return RespostaDeProblema
                .de(ProblemaDeBordaEmMensagem.montar(ProblemaDeBorda.RECURSO_EM_USO,
                        "O cartão está em uso por outra requisição. "
                                + "Confira o saldo antes de tentar de novo.",
                        requisicao.getRequestURI(), Map.of()));
    }
}
