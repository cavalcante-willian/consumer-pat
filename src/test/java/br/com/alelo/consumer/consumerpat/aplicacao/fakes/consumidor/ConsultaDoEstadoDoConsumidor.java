package br.com.alelo.consumer.consumerpat.aplicacao.fakes.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;

import java.util.List;
import java.util.Optional;

/**
 * Deixa o teste conferir o que está gravado e o que foi gravado em cada operação, sem passar pela
 * porta.
 */
public interface ConsultaDoEstadoDoConsumidor extends ComConsumidoresEmMemoria {

    /**
     * O consumidor como está gravado.
     *
     * @param id o id
     * @return o consumidor, ou vazio
     */
    default Optional<Consumidor> estado(ConsumidorId id) {
        return Optional.ofNullable(memoria().consumidores().get(id));
    }

    /**
     * O que foi gravado, na ordem, sem contar os consumidores que já existiam.
     *
     * @return cópia da lista de gravações
     */
    default List<Consumidor> gravados() {
        return List.copyOf(memoria().gravacoes());
    }
}
