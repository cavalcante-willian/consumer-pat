package br.com.alelo.consumer.consumerpat.api.configuracao;

import br.com.alelo.consumer.consumerpat.api.comum.seguranca.FiltroDeRejeicaoDeTrace;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.DispatcherType;

import java.util.EnumSet;

/**
 * Registra a recusa do {@code TRACE}, para qualquer rota e também para o despacho de erro, que é
 * onde o eco da requisição sai.
 *
 * <p>
 * A posição dele na ordem está em {@link OrdemDosFiltros}: depois da correlação e do log de
 * conclusão, para o {@code TRACE} recusado sair no log com o id da requisição.
 */
@Configuration
public class ConfiguracaoDoFiltroDeRejeicaoDeTrace {

    /**
     * Registra o filtro.
     *
     * @return o registro do filtro
     */
    @Bean
    public FilterRegistrationBean<FiltroDeRejeicaoDeTrace> filtroDeRejeicaoDeTrace() {
        var registro = new FilterRegistrationBean<>(new FiltroDeRejeicaoDeTrace());
        registro.addUrlPatterns("/*");
        registro.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST, DispatcherType.ERROR));
        registro.setOrder(OrdemDosFiltros.REJEICAO_DE_TRACE);
        return registro;
    }
}
