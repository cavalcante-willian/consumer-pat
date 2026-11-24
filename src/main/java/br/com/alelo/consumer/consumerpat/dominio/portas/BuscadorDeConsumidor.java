package br.com.alelo.consumer.consumerpat.dominio.portas;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado.ErroConsumidorNaoEncontrado;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

/**
 * Papel de quem sabe buscar um consumidor pelo id.
 *
 * <p>
 * A ausência é um {@link Resultado}, e não uma exceção nem um {@code null}: um id que não existe é
 * um desfecho esperado da consulta, e o chamador o trata como qualquer outro erro de domínio.
 */
public interface BuscadorDeConsumidor {

    /**
     * Busca o consumidor com todos os cartões dele.
     *
     * @param id o id procurado
     * @return o consumidor, ou {@link ErroConsumidorNaoEncontrado} se não existe
     */
    Resultado<Consumidor, ErroDominio> buscarPorId(ConsumidorId id);
}
