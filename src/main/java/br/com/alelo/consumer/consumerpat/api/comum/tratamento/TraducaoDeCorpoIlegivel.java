package br.com.alelo.consumer.consumerpat.api.comum.tratamento;

import br.com.alelo.consumer.consumerpat.api.comum.campos.ErrosDeCampoDoCorpo;
import br.com.alelo.consumer.consumerpat.api.comum.problema.ProblemaDeBorda;
import br.com.alelo.consumer.consumerpat.api.comum.problema.ProblemaDeBordaEmMensagem;
import br.com.alelo.consumer.consumerpat.api.comum.resposta.RespostaDeProblema;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * Papel do tratador que traduz o corpo que não pôde ser lido: o 400 com o campo, sem o texto do
 * Jackson.
 */
public interface TraducaoDeCorpoIlegivel {

    /**
     * Traduz a falha de leitura.
     *
     * @param excecao    a falha de leitura do corpo
     * @param requisicao a requisição que falhou
     * @return o 400 com o campo com erro, ou o corpo todo se não há campo
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    default ResponseEntity<Object> traduzirCorpoIlegivel(HttpMessageNotReadableException excecao,
            HttpServletRequest requisicao) {
        return RespostaDeProblema.de(ProblemaDeBordaEmMensagem.montar(
                ProblemaDeBorda.ENTRADA_INVALIDA, TraducaoDeEntradaInvalida.DETALHE_DE_CAMPOS,
                requisicao.getRequestURI(), ErrosDeCampoDoCorpo.de(excecao)));
    }
}
