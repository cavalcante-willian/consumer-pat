package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.dominio.erros.ErroDominio;
import br.com.alelo.consumer.consumerpat.dominio.erros.naoencontrado.ErroCartaoNaoEncontrado;
import br.com.alelo.consumer.consumerpat.dominio.portas.BuscadorDeCartaoParaAtualizacao;
import br.com.alelo.consumer.consumerpat.dominio.resultado.AusenciaComoErro;
import br.com.alelo.consumer.consumerpat.dominio.resultado.Resultado;

/**
 * A busca do cartão no banco, com trava de escrita até o fim da transação.
 *
 * <p>
 * Um cartão ausente, ou presente com outro tipo, é {@link ErroCartaoNaoEncontrado}, e nunca nulo. A
 * trava só se mantém dentro de uma transação, que é a do caso de uso: fora dela a leitura não
 * protege nada.
 */
interface BuscaDeCartaoNoBanco extends ComRepositorioDeCartoes, BuscadorDeCartaoParaAtualizacao {

    @Override
    default Resultado<Cartao, ErroDominio> buscarPorNumeroETipoParaAtualizacao(
            NumeroDeCartao numero, TipoCartao tipo) {
        return AusenciaComoErro.converter(
                repositorioDeCartoes().buscarParaAtualizacao(numero.digitos(), tipo)
                        .map(CartaoDeEntidade::converter),
                () -> new ErroCartaoNaoEncontrado(numero, tipo));
    }
}
