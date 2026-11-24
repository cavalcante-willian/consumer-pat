package br.com.alelo.consumer.consumerpat.api.comum.tratamento;

import br.com.alelo.consumer.consumerpat.api.comum.problema.ProblemaDeBorda;
import br.com.alelo.consumer.consumerpat.api.comum.problema.ProblemaDeBordaEmMensagem;
import br.com.alelo.consumer.consumerpat.api.comum.resposta.RespostaDeProblema;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Papel do tratador que traduz o método HTTP que a rota não aceita: o 405, com o cabeçalho
 * {@code Allow} dos métodos que ela aceita.
 */
public interface TraducaoDeMetodoNaoSuportado {

    /**
     * Traduz o método não suportado.
     *
     * @param excecao    o método que a rota não aceita
     * @param requisicao a requisição que falhou
     * @return o 405, com os métodos aceitos
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    default ResponseEntity<Object> traduzirMetodoNaoSuportado(
            HttpRequestMethodNotSupportedException excecao, HttpServletRequest requisicao) {
        var problema = ProblemaDeBordaEmMensagem.montar(ProblemaDeBorda.METODO_NAO_SUPORTADO,
                "O método não é suportado para esta rota.", requisicao.getRequestURI(), Map.of());
        var aceitos = Optional.ofNullable(excecao.getSupportedHttpMethods()).orElse(Set.of());
        var cabecalhos = new HttpHeaders();
        cabecalhos.setAllow(aceitos);
        return ResponseEntity.status(problema.status()).headers(cabecalhos)
                .contentType(RespostaDeProblema.TIPO_DE_MIDIA).body(problema);
    }
}
