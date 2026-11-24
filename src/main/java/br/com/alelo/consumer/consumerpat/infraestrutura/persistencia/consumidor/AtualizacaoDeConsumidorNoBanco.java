package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.consumidor.Consumidor;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado.ErroConsumidorNaoEncontrado;
import br.com.alelo.consumer.consumerpat.dominio.portas.AtualizadorDeConsumidor;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

/**
 * Grava no banco os dados cadastrais de um consumidor que já existe, e só eles.
 *
 * <p>
 * Os cartões e o saldo que vierem no consumidor são ignorados: a linha atualizada é a do banco, com
 * os dados novos, e os cartões devolvidos são os que estão gravados. É por isso que atualizar um
 * cadastro com um saldo velho não desfaz uma compra feita no meio.
 */
interface AtualizacaoDeConsumidorNoBanco
        extends ComRepositorioDeCartoes, DocumentoLivreNoBanco, AtualizadorDeConsumidor {

    @Override
    default Resultado<Consumidor, ErroDominio> atualizar(Consumidor consumidor) {
        return AusenciaComoErro
                .<ConsumidorEntidade, ErroDominio>converter(
                        repositorioDeConsumidores().findById(consumidor.id().valor()),
                        () -> new ErroConsumidorNaoEncontrado(consumidor.id()))
                .encadear(entidade -> VersaoDaLinhaConfirmada.exigir(entidade, consumidor))
                .encadear(entidade -> exigirDocumentoLivre(entidade, consumidor))
                .mapear(entidade -> repositorioDeConsumidores().saveAndFlush(
                        AtualizadorDeEntidadeDeConsumidor.atualizar(entidade, consumidor.dados())))
                .mapear(gravada -> ConsumidorDeEntidade.converter(gravada,
                        repositorioDeCartoes().buscarDoConsumidor(gravada.getId())));
    }
}
