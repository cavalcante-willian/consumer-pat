package br.com.alelo.consumer.consumerpat.api.comum.resposta;

import br.com.alelo.consumer.consumerpat.api.comum.problema.ProblemaDeErroDeDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.function.Function;

/**
 * Traduz o resultado da criação de um recurso que tem rota de leitura: {@code 201} com
 * {@code Location}, ou o problema do erro.
 *
 * <p>
 * É {@link RespostaDeResultado} para o recurso que o cliente pode ler depois de criar: o sucesso
 * aponta onde. A criação de um comprovante, que não tem rota de leitura, segue sem
 * {@code Location}.
 */
public final class RespostaDeResultadoCriado {

    private RespostaDeResultadoCriado() {
    }

    /**
     * Monta a resposta.
     *
     * @param resultado       o que o caso de uso devolveu
     * @param comoMensagem    como o valor criado vira a mensagem de saída
     * @param comoLocalizacao como o valor criado vira o endereço de leitura
     * @param instancia       o caminho da requisição, para o problema
     * @param <T>             o tipo do valor criado
     * @return {@code 201} com o corpo e o {@code Location}, ou o problema com o status da categoria
     */
    public static <T> ResponseEntity<Object> responder(Resultado<T, ErroDominio> resultado,
            Function<T, ?> comoMensagem, Function<T, URI> comoLocalizacao, String instancia) {
        return resultado.<ResponseEntity<Object>>resolver(
                valor -> ResponseEntity.created(comoLocalizacao.apply(valor))
                        .body(comoMensagem.apply(valor)),
                erro -> RespostaDeProblema.de(ProblemaDeErroDeDominio.montar(erro, instancia)));
    }
}
