package br.com.alelo.consumer.consumerpat.api.configuracao;

import br.com.alelo.consumer.consumerpat.InicializacaoDaAplicacao;
import br.com.alelo.consumer.consumerpat.api.comum.documentacao.InformacoesDaApi;
import br.com.alelo.consumer.consumerpat.api.comum.documentacao.RespostasDeErroDaApi;
import br.com.alelo.consumer.consumerpat.api.comum.documentacao.TagsDaApi;
import br.com.alelo.consumer.consumerpat.api.comum.documentacao.VersaoDaAplicacao;
import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Liga a documentação da API: o cabeçalho da especificação, os grupos de operações e o que é comum
 * às respostas de erro.
 *
 * <p>
 * Só compõe: o conteúdo de cada parte vive na sua própria classe, em
 * {@code api.comum.documentacao}. A especificação sai em {@code /v3/api-docs} e a página do Swagger
 * em {@code /swagger-ui}, que o perfil {@code prod} desliga.
 */
@Configuration
public class ConfiguracaoDaDocumentacao {

    /**
     * A especificação, com o cabeçalho e os grupos de operações.
     *
     * @return a especificação base, que o springdoc completa com as rotas
     */
    @Bean
    public OpenAPI especificacao() {
        return new OpenAPI()
                .info(InformacoesDaApi.montar(VersaoDaAplicacao.de(InicializacaoDaAplicacao.class)))
                .tags(TagsDaApi.lista());
    }

    /**
     * O que vale para todas as operações: o corpo dos erros, o {@code 500} e o
     * {@code X-Request-Id}.
     *
     * @return o customizador aplicado pelo springdoc depois de montar as rotas
     */
    @Bean
    public OpenApiCustomiser respostasDeErro() {
        return new RespostasDeErroDaApi();
    }
}
