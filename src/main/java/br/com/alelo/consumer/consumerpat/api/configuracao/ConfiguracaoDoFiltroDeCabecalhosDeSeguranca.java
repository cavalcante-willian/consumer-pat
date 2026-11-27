package br.com.alelo.consumer.consumerpat.api.configuracao;

import br.com.alelo.consumer.consumerpat.api.comum.seguranca.FiltroDeCabecalhosDeSeguranca;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra os cabeçalhos de segurança, o filtro mais externo, só para {@code /api/*}.
 *
 * <p>
 * Não vale para o Swagger nem para o console do H2, que a política de conteúdo quebraria. A posição
 * dele na ordem está em {@link OrdemDosFiltros}.
 */
@Configuration
public class ConfiguracaoDoFiltroDeCabecalhosDeSeguranca {

    /**
     * Registra o filtro.
     *
     * @return o registro do filtro
     */
    @Bean
    public FilterRegistrationBean<FiltroDeCabecalhosDeSeguranca> filtroDeCabecalhosDeSeguranca() {
        var registro = new FilterRegistrationBean<>(new FiltroDeCabecalhosDeSeguranca());
        registro.addUrlPatterns("/api/*");
        registro.setOrder(OrdemDosFiltros.CABECALHOS_DE_SEGURANCA);
        return registro;
    }
}
