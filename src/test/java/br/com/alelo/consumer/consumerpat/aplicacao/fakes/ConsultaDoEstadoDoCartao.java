package br.com.alelo.consumer.consumerpat.aplicacao.fakes;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;

import java.util.Optional;

/**
 * Papel do fake que deixa o teste ver o estado final de um cartão. Não existe na porta real: é só
 * para conferir.
 */
public interface ConsultaDoEstadoDoCartao extends ComCartoesEmMemoria {

    /**
     * O cartão como está agora.
     *
     * @param numero o número do cartão
     * @param tipo   o tipo do cartão
     * @return o cartão, ou vazio se não existe
     */
    default Optional<Cartao> estado(NumeroDeCartao numero, TipoCartao tipo) {
        return Optional.ofNullable(memoria().cartoes().get(new ChaveDeCartao(numero, tipo)));
    }
}
