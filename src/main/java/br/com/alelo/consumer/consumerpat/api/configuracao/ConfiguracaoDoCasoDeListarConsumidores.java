package br.com.alelo.consumer.consumerpat.api.configuracao;

// editorconfig-checker-disable-next-line
import br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.consumidor.listar.ListarConsumidoresCaso;
import br.com.alelo.consumer.consumerpat.dominio.portas.ConsumidorPorta;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expõe o caso de uso de listar os consumidores como bean, o que ativa o seu
 * {@code @Transactional(readOnly = true)}.
 */
@Configuration
public class ConfiguracaoDoCasoDeListarConsumidores {

    /**
     * Cria o caso de uso com a porta do consumidor.
     *
     * @param consumidorPorta a porta, que a composição liga ao adaptador
     * @return o caso de uso
     */
    @Bean
    public ListarConsumidoresCaso listarConsumidoresCaso(ConsumidorPorta consumidorPorta) {
        return new ListarConsumidoresCaso(consumidorPorta);
    }
}
