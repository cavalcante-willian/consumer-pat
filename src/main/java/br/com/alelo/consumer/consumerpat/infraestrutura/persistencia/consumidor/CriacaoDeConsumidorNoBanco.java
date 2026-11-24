package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.NovoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.portas.GravadorDeNovoConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Falha;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Sucesso;

/**
 * Cria o consumidor no banco: se colide, devolve o conflito; senão, grava.
 */
interface CriacaoDeConsumidorNoBanco extends ConflitoDeNovoConsumidorNoBanco,
        GravacaoDoNovoConsumidorNoBanco, GravadorDeNovoConsumidor {

    @Override
    default Resultado<Consumidor, ErroDominio> criar(NovoConsumidor novo) {
        return conflitoDe(novo).<Resultado<Consumidor, ErroDominio>>map(Falha::new)
                .orElseGet(() -> new Sucesso<>(gravar(novo)));
    }
}
