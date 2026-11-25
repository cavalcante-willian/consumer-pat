package br.com.alelo.consumer.consumerpat.api.configuracao;

import br.com.alelo.consumer.consumerpat.api.comum.requisicao.FiltroDeCorrelacao;
import br.com.alelo.consumer.consumerpat.api.comum.requisicao.FiltroDeLogDeConclusao;
import br.com.alelo.consumer.consumerpat.dominio.log.SaidaDeLogPorta;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Registra os filtros da requisição, na ordem do contrato.
 *
 * <p>
 * A ordem é parte do contrato, e menor número é mais externo: a correlação vem primeiro, para que o
 * log de conclusão, logo dentro dela, ainda escreva com o id. Trocá-los é mudança revisada. O
 * número {@code HIGHEST_PRECEDENCE} está reservado ao filtro dos cabeçalhos de segurança, que é o
 * mais externo e protege toda resposta, inclusive as de erro.
 */
@Configuration
public class ConfiguracaoDeFiltros {

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
}
