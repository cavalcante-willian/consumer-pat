package br.com.alelo.consumer.consumerpat.api.configuracao;

// editorconfig-checker-disable-next-line
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.atualizar.AtualizarConsumidorCaso;
import br.com.alelo.consumer.consumerpat.dominio.log.SaidaDeLogPorta;
import br.com.alelo.consumer.consumerpat.dominio.portas.ConsumidorPorta;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expõe o caso de uso de atualizar o cadastro como bean, o que ativa o seu {@code @Transactional}.
 */
@Configuration
public class ConfiguracaoDoCasoDeAtualizarConsumidor {

    /**
     * Cria o caso de uso com a porta do consumidor, que serve de busca e de atualização.
     *
     * @param consumidorPorta a porta, que a composição liga ao adaptador
     * @param saida           a porta de log
     * @return o caso de uso
     */
    @Bean
    public AtualizarConsumidorCaso atualizarConsumidorCaso(ConsumidorPorta consumidorPorta,
            SaidaDeLogPorta saida) {
        return new AtualizarConsumidorCaso(consumidorPorta, consumidorPorta, saida);
    }
}
