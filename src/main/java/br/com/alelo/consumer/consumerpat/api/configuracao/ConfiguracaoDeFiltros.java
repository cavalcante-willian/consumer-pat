package br.com.alelo.consumer.consumerpat.api.configuracao;

import br.com.alelo.consumer.consumerpat.api.comum.requisicao.FiltroDeCorrelacao;
import br.com.alelo.consumer.consumerpat.api.comum.requisicao.FiltroDeLogDeConclusao;
import br.com.alelo.consumer.consumerpat.api.comum.seguranca.FiltroDeCabecalhosDeSeguranca;
import br.com.alelo.consumer.consumerpat.api.comum.seguranca.FiltroDeRejeicaoDeTrace;
import br.com.alelo.consumer.consumerpat.dominio.log.SaidaDeLogPorta;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import javax.servlet.DispatcherType;

import java.util.EnumSet;

/**
 * Registra os filtros da requisição, na ordem do contrato.
 *
 * <p>
 * A ordem é parte do contrato, e menor número é mais externo: os cabeçalhos de segurança vêm
 * primeiro, para proteger toda resposta, inclusive as de erro; depois a correlação, e só então o
 * log de conclusão, que logo dentro dela ainda escreve com o id; por último a recusa do
 * {@code TRACE}, para o método recusado também sair no log. Trocá-los é mudança revisada.
 */
@Configuration
public class ConfiguracaoDeFiltros {

    /**
     * Registra os cabeçalhos de segurança, a etapa 1, só para {@code /api/*}.
     *
     * <p>
     * Não vale para o Swagger nem para o console do H2, que a política de conteúdo quebraria.
     *
     * @return o registro do filtro
     */
    @Bean
    public FilterRegistrationBean<FiltroDeCabecalhosDeSeguranca> filtroDeCabecalhosDeSeguranca() {
        var registro = new FilterRegistrationBean<>(new FiltroDeCabecalhosDeSeguranca());
        registro.addUrlPatterns("/api/*");
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registro;
    }

    /**
     * Registra a correlação, a etapa 2.
     *
     * @return o registro do filtro
     */
    @Bean
    public FilterRegistrationBean<FiltroDeCorrelacao> filtroDeCorrelacao() {
        var registro = new FilterRegistrationBean<>(new FiltroDeCorrelacao());
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registro;
    }

    /**
     * Registra o log de conclusão, a etapa 3.
     *
     * @param saida a porta de log
     * @return o registro do filtro
     */
    @Bean
    public FilterRegistrationBean<FiltroDeLogDeConclusao> filtroDeLogDeConclusao(
            SaidaDeLogPorta saida) {
        var registro = new FilterRegistrationBean<>(new FiltroDeLogDeConclusao(saida));
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        return registro;
    }

    /**
     * Registra a recusa do {@code TRACE}, a etapa 4, para qualquer rota e também para o despacho de
     * erro, que é onde o eco da requisição sai.
     *
     * <p>
     * Vem depois da correlação e do log de conclusão, para o {@code TRACE} recusado sair no log com
     * o id da requisição.
     *
     * @return o registro do filtro
     */
    @Bean
    public FilterRegistrationBean<FiltroDeRejeicaoDeTrace> filtroDeRejeicaoDeTrace() {
        var registro = new FilterRegistrationBean<>(new FiltroDeRejeicaoDeTrace());
        registro.addUrlPatterns("/*");
        registro.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST, DispatcherType.ERROR));
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE + 3);
        return registro;
    }
}
