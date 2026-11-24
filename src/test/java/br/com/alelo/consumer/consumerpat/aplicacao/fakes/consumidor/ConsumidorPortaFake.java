package br.com.alelo.consumer.consumerpat.aplicacao.fakes.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.portas.ConsumidorPorta;

import java.util.Arrays;

/**
 * A porta do consumidor em memória: só guarda o estado e declara os papéis.
 *
 * <p>
 * Obedece o mesmo contrato da porta real, e o {@code ConsumidorPortaFakeTeste} o prova. O que ele
 * devolve para um consumidor ausente, um documento repetido e uma versão velha é o que a
 * infraestrutura devolve, de modo que o teste de um caso de uso não passa por um motivo falso.
 */
public class ConsumidorPortaFake implements ConsumidorPorta, CriacaoDeConsumidorEmMemoria,
        AtualizacaoDeConsumidorEmMemoria, ConsultaDoEstadoDoConsumidor {

    private final ConsumidoresEmMemoria memoria = new ConsumidoresEmMemoria();

    /**
     * Cria o fake com os consumidores que já existiam.
     *
     * @param iniciais os consumidores presentes antes do teste
     */
    public ConsumidorPortaFake(Consumidor... iniciais) {
        Arrays.stream(iniciais)
                .forEach(consumidor -> memoria.consumidores().put(consumidor.id(), consumidor));
    }

    @Override
    public ConsumidoresEmMemoria memoria() {
        return memoria;
    }
}
