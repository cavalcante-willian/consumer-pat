package br.com.alelo.consumer.consumerpat.api.configuracao;

import br.com.alelo.consumer.consumerpat.dominio.portas.CartaoPorta;
import br.com.alelo.consumer.consumerpat.dominio.portas.ExtratoPorta;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.CartaoAdaptador;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ExtratoAdaptador;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Clock;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova que a composition root liga o que promete: cada porta do domínio tem o seu adaptador e o
 * relógio da aplicação é o de parede, em UTC.
 */
@SpringBootTest
class ConfiguracaoDePortasTeste {

    @Autowired
    private CartaoPorta cartaoPorta;

    @Autowired
    private ExtratoPorta extratoPorta;

    @Autowired
    private Clock relogio;

    @Test
    void deve_ligar_a_porta_do_cartao_ao_adaptador_quando_subir_o_contexto() {
        // Verificar
        assertThat(cartaoPorta).isInstanceOf(CartaoAdaptador.class);
    }

    @Test
    void deve_ligar_a_porta_do_extrato_ao_adaptador_quando_subir_o_contexto() {
        // Verificar
        assertThat(extratoPorta).isInstanceOf(ExtratoAdaptador.class);
    }

    @Test
    void deve_usar_o_relogio_de_parede_em_utc_quando_subir_o_contexto() {
        // Verificar
        assertThat(relogio.getZone()).isEqualTo(ZoneOffset.UTC);
    }
}
