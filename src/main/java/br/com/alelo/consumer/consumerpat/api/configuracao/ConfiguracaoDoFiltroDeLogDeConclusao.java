package br.com.alelo.consumer.consumerpat.api.configuracao;

import br.com.alelo.consumer.consumerpat.api.comum.requisicao.FiltroDeLogDeConclusao;
import br.com.alelo.consumer.consumerpat.dominio.log.SaidaDeLogPorta;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra o log de conclusão, que escreve uma linha por requisição, com o status e a duração.
 *
 * <p>
 * A posição dele na ordem está em {@link OrdemDosFiltros}: logo dentro da correlação, para a linha
 * sair com o id da requisição.
 */
@Configuration
public class ConfiguracaoDoFiltroDeLogDeConclusao {

    /**
     * Registra o filtro.
     *
     * @param saida a porta de log
     * @return o registro do filtro
     */
    @Bean
    public FilterRegistrationBean<FiltroDeLogDeConclusao> filtroDeLogDeConclusao(
            SaidaDeLogPorta saida) {
        var registro = new FilterRegistrationBean<>(new FiltroDeLogDeConclusao(saida));
        registro.setOrder(OrdemDosFiltros.LOG_DE_CONCLUSAO);
        return registro;
    }
}
