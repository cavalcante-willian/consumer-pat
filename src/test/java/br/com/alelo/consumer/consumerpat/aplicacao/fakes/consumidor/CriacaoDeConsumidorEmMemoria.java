package br.com.alelo.consumer.consumerpat.aplicacao.fakes.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.NovoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.portas.GravadorDeNovoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;

/**
 * Grava o consumidor novo em memória, como a porta real: dá o id e a versão zero, e devolve o
 * conflito de documento ou de número de cartão em vez de gravar.
 */
public interface CriacaoDeConsumidorEmMemoria
        extends ConflitoDeNovoConsumidorEmMemoria, GravadorDeNovoConsumidor {

    @Override
    default Resultado<Consumidor, ErroDominio> criar(NovoConsumidor novo) {
        return conflitoDe(novo).<Resultado<Consumidor, ErroDominio>>map(Falha::new)
                .orElseGet(() -> {
                    var gravado = new Consumidor(new ConsumidorId(memoria().proximoId()),
                            novo.dados(), novo.cartoes(), 0);
                    memoria().consumidores().put(gravado.id(), gravado);
                    memoria().gravacoes().add(gravado);
                    return new Sucesso<>(gravado);
                });
    }
}
