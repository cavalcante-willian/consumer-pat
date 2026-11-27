package br.com.alelo.consumer.consumerpat.api.configuracao;

import br.com.alelo.consumer.consumerpat.api.comum.documentacao.RespostasDeErroDaApi;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Liga o que é comum às respostas de erro na especificação: o corpo de problema, o {@code 500} e o
 * {@code X-Request-Id}, em toda operação.
 *
 * <p>
 * Só compõe: o que o customizador faz vive em {@link RespostasDeErroDaApi}.
 */
@Configuration
public class ConfiguracaoDasRespostasDeErroDaApi {

    /**
     * O customizador das respostas de erro.
     *
     * @return o customizador aplicado pelo springdoc depois de montar as rotas
     */
    @Bean
    public OpenApiCustomiser respostasDeErro() {
        return new RespostasDeErroDaApi();
    }
}
