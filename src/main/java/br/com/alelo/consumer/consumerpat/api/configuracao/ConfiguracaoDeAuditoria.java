package br.com.alelo.consumer.consumerpat.api.configuracao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Clock;
import java.util.Optional;

/**
 * Liga a auditoria automática do banco: {@code data_criacao} e {@code data_atualizacao} são
 * preenchidas pelo mecanismo, e nenhum caso de uso as atribui.
 *
 * <p>
 * O instante vem do mesmo {@link Clock} do resto da aplicação, e não do relógio do sistema, o que
 * permite ao teste fixar o tempo e provar que a data de atualização avança.
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "provedorDeInstante")
public class ConfiguracaoDeAuditoria {

    /**
     * Quem diz à auditoria que horas são.
     *
     * @param relogio o relógio da aplicação
     * @return um provedor que devolve o instante atual do relógio
     */
    @Bean
    public DateTimeProvider provedorDeInstante(Clock relogio) {
        return () -> Optional.of(relogio.instant());
    }
}
