package br.com.alelo.consumer.consumerpat.api.comum.tratamento;

import br.com.alelo.consumer.consumerpat.api.comum.problema.ProblemaDeBorda;
import br.com.alelo.consumer.consumerpat.api.comum.problema.ProblemaDeBordaEmMensagem;
import br.com.alelo.consumer.consumerpat.api.comum.resposta.RespostaDeProblema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * Papel do tratador que traduz o tipo de mídia que a rota não aceita: o 415 padronizado.
 */
public interface TraducaoDeMidiaNaoSuportada {

    /**
     * Traduz o tipo de mídia não suportado.
     *
     * @param excecao    o tipo de mídia que a rota não aceita
     * @param requisicao a requisição que falhou
     * @return o 415
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    default ResponseEntity<Object> traduzirMidiaNaoSuportada(
            HttpMediaTypeNotSupportedException excecao, HttpServletRequest requisicao) {
        return RespostaDeProblema
                .de(ProblemaDeBordaEmMensagem.montar(ProblemaDeBorda.MIDIA_NAO_SUPORTADA,
                        "O tipo de mídia do corpo não é suportado: envie application/json.",
                        requisicao.getRequestURI(), Map.of()));
    }
}
