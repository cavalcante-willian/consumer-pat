package br.com.alelo.consumer.consumerpat.api.configuracao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Escolhe o relógio da aplicação: o de parede, em UTC.
 *
 * <p>
 * É o único relógio. O domínio, os casos de uso e a auditoria do banco recebem este mesmo bean, de
 * modo que um teste que o troque por um relógio fixo controla o tempo de ponta a ponta. Nenhum
 * código chama {@code Instant.now()} por conta própria.
 */
@Configuration
public class ConfiguracaoDeRelogio {

    /**
     * O relógio da aplicação.
     *
     * @return o relógio do sistema, em UTC
     */
    @Bean
    public Clock relogio() {
        return Clock.systemUTC();
    }
}
