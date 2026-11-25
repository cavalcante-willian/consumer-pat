package br.com.alelo.consumer.consumerpat.api.comum.tratamento;

import br.com.alelo.consumer.consumerpat.api.comum.problema.ProblemaDeBorda;
import br.com.alelo.consumer.consumerpat.api.comum.problema.ProblemaDeBordaEmMensagem;
import br.com.alelo.consumer.consumerpat.api.comum.resposta.RespostaDeProblema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

/**
 * Traduz um valor de rota ou de parâmetro que não é do tipo esperado em {@code 400}.
 *
 * <p>
 * É o {@code /api/v1/consumidores/abc}: o id precisa ser número. Aponta o nome do parâmetro e diz o
 * que se espera, e nunca repete o valor recebido nem o nome do tipo Java.
 */
public interface TraducaoDeArgumentoInvalido {

    /**
     * Traduz o argumento de tipo errado.
     *
     * @param excecao    o argumento que não converteu
     * @param requisicao a requisição que o trouxe
     * @return o problema {@code 400}, com o parâmetro apontado
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    default ResponseEntity<Object> traduzirArgumentoInvalido(
            MethodArgumentTypeMismatchException excecao, HttpServletRequest requisicao) {
        return RespostaDeProblema
                .de(ProblemaDeBordaEmMensagem.montar(ProblemaDeBorda.ENTRADA_INVALIDA,
                        TraducaoDeEntradaInvalida.DETALHE_DE_CAMPOS, requisicao.getRequestURI(),
                        Map.of(excecao.getName(), List.of("não tem o formato esperado"))));
    }
}
