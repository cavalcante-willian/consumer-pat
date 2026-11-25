package br.com.alelo.consumer.consumerpat.api.configuracao;

import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.obter.ObterConsumidorCaso;
import br.com.alelo.consumer.consumerpat.dominio.portas.ConsumidorPorta;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expõe o caso de uso de ler o consumidor como bean, o que ativa o seu
 * {@code @Transactional(readOnly = true)}.
 */
@Configuration
public class ConfiguracaoDoCasoDeObterConsumidor {

    /**
     * Cria o caso de uso com a porta do consumidor.
     *
     * @param consumidorPorta a porta, que a composição liga ao adaptador
     * @return o caso de uso
     */
    @Bean
    public ObterConsumidorCaso obterConsumidorCaso(ConsumidorPorta consumidorPorta) {
        return new ObterConsumidorCaso(consumidorPorta);
    }
}
