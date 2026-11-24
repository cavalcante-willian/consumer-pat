package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cadastro;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.consumidor.ConsumidorId;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado.ErroConsumidorNaoEncontrado;
import br.com.alelo.consumer.consumerpat.dominio.portas.BuscadorDeConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao.ComRepositorioDeCartoes;
// editorconfig-checker-disable-next-line
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ComRepositorioDeConsumidores;

/**
 * Busca o consumidor no banco, com os cartões dele.
 *
 * <p>
 * São duas consultas por consumidor, a da linha e a dos cartões, e não uma por cartão. A leitura
 * não trava: quem trava é a busca do cartão, que é o caminho do saldo.
 */
interface BuscaDeConsumidorNoBanco
        extends ComRepositorioDeConsumidores, ComRepositorioDeCartoes, BuscadorDeConsumidor {

    @Override
    default Resultado<Consumidor, ErroDominio> buscarPorId(ConsumidorId id) {
        return AusenciaComoErro
                .converter(
                        repositorioDeConsumidores().findById(id.valor())
                                .map(entidade -> ConsumidorDasLinhas.converter(entidade,
                                        repositorioDeCartoes()
                                                .buscarDoConsumidor(entidade.getId()))),
                        () -> new ErroConsumidorNaoEncontrado(id));
    }
}
