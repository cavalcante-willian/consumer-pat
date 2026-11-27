package br.com.alelo.consumer.consumerpat.api.configuracao;

import br.com.alelo.consumer.consumerpat.InicializacaoDaAplicacao;
import br.com.alelo.consumer.consumerpat.api.comum.documentacao.InformacoesDaApi;
import br.com.alelo.consumer.consumerpat.api.comum.documentacao.TagsDaApi;
import br.com.alelo.consumer.consumerpat.api.comum.documentacao.VersaoDaAplicacao;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Liga o cabeçalho da especificação OpenAPI e os grupos de operações.
 *
 * <p>
 * Só compõe: o conteúdo de cada parte vive na sua própria classe, em
 * {@code api.comum.documentacao}. A especificação sai em {@code /v3/api-docs} e a página do Swagger
 * em {@code /swagger-ui}, que o perfil {@code prod} desliga.
 */
@Configuration
public class ConfiguracaoDaEspecificacaoDaApi {

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
}
