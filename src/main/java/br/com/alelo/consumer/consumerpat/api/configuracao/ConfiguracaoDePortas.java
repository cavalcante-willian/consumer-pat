package br.com.alelo.consumer.consumerpat.api.configuracao;

import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.CartaoAdaptador;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ExtratoAdaptador;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Escolhe a implementação de cada porta do domínio.
 *
 * <p>
 * É o único lugar que nomeia os adaptadores. Eles entram por importação da classe, e não por um
 * método de fábrica, porque os repositórios que recebem são pacote-privados e esta classe não os
 * enxerga: o Spring os injeta de dentro do pacote, e a API só decide quais adaptadores existem.
 */
@Configuration
@Import({ CartaoAdaptador.class, ExtratoAdaptador.class })
public class ConfiguracaoDePortas {
}
