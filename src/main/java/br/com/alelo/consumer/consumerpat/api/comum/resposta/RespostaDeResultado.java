package br.com.alelo.consumer.consumerpat.api.comum.resposta;

import br.com.alelo.consumer.consumerpat.api.comum.problema.ProblemaDeErroDeDominio;

import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.function.Function;

/**
 * Transforma o resultado de um caso de uso na resposta HTTP.
 *
 * <p>
 * O sucesso sai com o status que o endpoint declarou e o corpo na mensagem de fronteira, e a falha
 * sai como corpo de erro, com o status da categoria do erro. Quem escolhe o status da falha é o
 * enum da categoria, e nunca o controller: o resultado se resolve pelos dois caminhos, sem
 * ramificação.
 */
public final class RespostaDeResultado {

    private RespostaDeResultado() {
    }

    /**
     * Monta a resposta.
     *
     * @param resultado    o resultado do caso de uso
     * @param comoMensagem como o valor do sucesso vira a mensagem de fronteira
     * @param sucesso      o status da resposta de sucesso
     * @param instancia    o caminho da requisição, para o corpo de erro
     * @param <T>          o tipo do valor do sucesso
     * @return a resposta de sucesso ou de erro
     */
    public static <T> ResponseEntity<Object> responder(Resultado<T, ErroDominio> resultado,
            Function<T, ?> comoMensagem, HttpStatus sucesso, String instancia) {
        return resultado.<ResponseEntity<Object>>resolver(
                valor -> ResponseEntity.status(sucesso).body(comoMensagem.apply(valor)),
                erro -> RespostaDeProblema.de(ProblemaDeErroDeDominio.montar(erro, instancia)));
    }
}
