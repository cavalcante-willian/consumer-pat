package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

import br.com.alelo.consumer.consumerpat.fabricas.RelogioAjustavel;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Instant;

/**
 * O relógio dos testes de persistência: um {@link RelogioAjustavel}, que a auditoria usa como se
 * fosse o relógio da aplicação.
 */
@TestConfiguration
class ConfiguracaoDeRelogioDeTeste {

    /** O instante em que todo teste de persistência começa. */
    static final Instant INICIO = Instant.parse("2026-11-24T12:00:00Z");

    @Bean
    RelogioAjustavel relogio() {
        return new RelogioAjustavel(INICIO);
    }
}
