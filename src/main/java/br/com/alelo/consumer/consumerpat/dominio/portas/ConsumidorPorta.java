package br.com.alelo.consumer.consumerpat.dominio.portas;

/**
 * A porta do consumidor: buscar, criar e atualizar.
 *
 * <p>
 * Só compõe os papéis, e cada um tem o seu arquivo. Um caso de uso que só lê depende de
 * {@link BuscadorDeConsumidor}, e não da porta inteira, e por isso o seu teste só precisa de um
 * fake com uma operação.
 */
public interface ConsumidorPorta
        extends BuscadorDeConsumidor, GravadorDeNovoConsumidor, AtualizadorDeConsumidor {
}
