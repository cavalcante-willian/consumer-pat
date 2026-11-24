package br.com.alelo.consumer.consumerpat.aplicacao.fakes;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado.ErroCartaoNaoEncontrado;
import br.com.alelo.consumer.consumerpat.dominio.portas.BuscadorDeCartaoParaAtualizacao;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

import java.util.Optional;

/**
 * A busca do cartão em memória, com o contrato da porta real: um cartão ausente, ou presente com
 * outro tipo, é {@link ErroCartaoNaoEncontrado}, e nunca nulo. Não trava nada, porque não há
 * concorrência num teste unitário.
 */
public interface BuscaDeCartaoEmMemoria
        extends ComCartoesEmMemoria, BuscadorDeCartaoParaAtualizacao {

    @Override
    default Resultado<Cartao, ErroDominio> buscarPorNumeroETipoParaAtualizacao(
            NumeroDeCartao numero, TipoCartao tipo) {
        return AusenciaComoErro.converter(
                Optional.ofNullable(memoria().cartoes().get(new ChaveDeCartao(numero, tipo))),
                () -> new ErroCartaoNaoEncontrado(numero, tipo));
    }
}
