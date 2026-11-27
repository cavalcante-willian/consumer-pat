package br.com.alelo.consumer.consumerpat.api.configuracao;

import br.com.alelo.consumer.consumerpat.api.comum.requisicao.FiltroDeCorrelacao;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra a correlação, que dá a cada requisição o seu id.
 *
 * <p>
 * A posição dela na ordem está em {@link OrdemDosFiltros}: logo depois dos cabeçalhos de segurança,
 * e antes do log de conclusão, que escreve com o id que ela põe.
 */
@Configuration
public class ConfiguracaoDoFiltroDeCorrelacao {

    /**
     * Registra o filtro.
     *
     * @return o registro do filtro
     */
    @Bean
    public FilterRegistrationBean<FiltroDeCorrelacao> filtroDeCorrelacao() {
        var registro = new FilterRegistrationBean<>(new FiltroDeCorrelacao());
        registro.setOrder(OrdemDosFiltros.CORRELACAO);
        return registro;
    }
}
