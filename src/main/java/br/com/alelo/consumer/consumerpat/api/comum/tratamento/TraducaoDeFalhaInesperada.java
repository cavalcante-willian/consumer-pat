package br.com.alelo.consumer.consumerpat.api.comum.tratamento;

import br.com.alelo.consumer.consumerpat.api.comum.problema.ProblemaDeBorda;
import br.com.alelo.consumer.consumerpat.api.comum.problema.ProblemaDeBordaEmMensagem;
import br.com.alelo.consumer.consumerpat.api.comum.resposta.RespostaDeProblema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * Papel do tratador que traduz o que ninguém previu: o 500, sem dizer o que foi.
 *
 * <p>
 * É a última rede, e a única que captura {@code Exception}: o tipo exato de cada falha esperada já
 * tem o seu papel, e o framework escolhe sempre o mais específico. O corpo nunca leva a mensagem da
 * exceção, que pode citar SQL, nome de tabela ou pilha. Registrar a causa em log é do item de
 * observabilidade; até lá, a falha chega ao cliente sem rastro no servidor.
 */
public interface TraducaoDeFalhaInesperada {

    /**
     * Traduz a falha inesperada.
     *
     * @param excecao    o que falhou
     * @param requisicao a requisição que falhou
     * @return o 500 genérico
     */
    @ExceptionHandler(Exception.class)
    default ResponseEntity<Object> traduzirFalhaInesperada(Exception excecao,
            HttpServletRequest requisicao) {
        return RespostaDeProblema
                .de(ProblemaDeBordaEmMensagem.montar(ProblemaDeBorda.FALHA_INESPERADA,
                        "Ocorreu uma falha inesperada.", requisicao.getRequestURI(), Map.of()));
    }
}
