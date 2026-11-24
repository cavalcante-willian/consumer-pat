package br.com.alelo.consumer.consumerpat.aplicacao.fakes.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado.ErroConsumidorNaoEncontrado;
import br.com.alelo.consumer.consumerpat.dominio.portas.BuscadorDeConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

import java.util.Optional;

/**
 * Busca o consumidor em memória, com a ausência como o erro que a porta real devolve.
 */
public interface BuscaDeConsumidorEmMemoria extends ComConsumidoresEmMemoria, BuscadorDeConsumidor {

    @Override
    default Resultado<Consumidor, ErroDominio> buscarPorId(ConsumidorId id) {
        return AusenciaComoErro.converter(Optional.ofNullable(memoria().consumidores().get(id)),
                () -> new ErroConsumidorNaoEncontrado(id));
    }
}
