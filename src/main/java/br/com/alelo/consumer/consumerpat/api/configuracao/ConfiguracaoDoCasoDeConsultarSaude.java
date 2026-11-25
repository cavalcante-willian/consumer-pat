package br.com.alelo.consumer.consumerpat.api.configuracao;

import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.saude.ConsultarSaudeCaso;
import br.com.alelo.consumer.consumerpat.dominio.saude.SaudePorta;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expõe o caso de uso de consultar a saúde como bean.
 */
@Configuration
public class ConfiguracaoDoCasoDeConsultarSaude {

    /**
     * Cria o caso de uso com a porta de saúde.
     *
     * @param saude a porta de saúde, que a composição liga ao adaptador
     * @return o caso de uso
     */
    @Bean
    public ConsultarSaudeCaso consultarSaudeCaso(SaudePorta saude) {
        return new ConsultarSaudeCaso(saude);
    }
}
