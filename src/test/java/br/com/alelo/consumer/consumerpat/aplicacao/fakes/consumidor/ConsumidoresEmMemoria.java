package br.com.alelo.consumer.consumerpat.aplicacao.fakes.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * O estado compartilhado dos fakes do consumidor: os consumidores gravados, o que foi gravado em
 * cada operação e a sequência dos ids.
 *
 * <p>
 * Os papéis do fake leem e escrevem aqui, e o fake só o guarda, de modo que nenhum papel é dono do
 * estado dos outros.
 */
public final class ConsumidoresEmMemoria {

    private final Map<ConsumidorId, Consumidor> consumidores = new HashMap<>();

    private final List<Consumidor> gravacoes = new ArrayList<>();

    private final AtomicLong sequencia = new AtomicLong();

    /**
     * Os consumidores presentes, por id.
     *
     * @return o mapa mutável
     */
    public Map<ConsumidorId, Consumidor> consumidores() {
        return consumidores;
    }

    /**
     * Cada consumidor gravado, na ordem, sem contar os que já existiam.
     *
     * @return a lista mutável
     */
    public List<Consumidor> gravacoes() {
        return gravacoes;
    }

    /**
     * Dá o próximo id, como a identidade do banco.
     *
     * @return o id, a partir de 1
     */
    public long proximoId() {
        return sequencia.incrementAndGet();
    }
}
