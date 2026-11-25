package br.com.alelo.consumer.consumerpat.api.configuracao;

import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.criar.CriarConsumidorCaso;
import br.com.alelo.consumer.consumerpat.dominio.portas.ConsumidorPorta;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expõe o caso de uso de cadastrar o consumidor como bean.
 *
 * <p>
 * É o bean que ativa o {@code @Transactional} do caso de uso: a camada de aplicação não conhece o
 * Spring, e é aqui que ela ganha o ciclo de vida. Uma configuração por caso de uso, cada uma no seu
 * arquivo.
 */
@Configuration
public class ConfiguracaoDoCasoDeCriarConsumidor {

    /**
     * Cria o caso de uso com a porta do consumidor.
     *
     * @param consumidorPorta a porta, que a composição liga ao adaptador
     * @return o caso de uso
     */
    @Bean
    public CriarConsumidorCaso criarConsumidorCaso(ConsumidorPorta consumidorPorta) {
        return new CriarConsumidorCaso(consumidorPorta);
    }
}
