package br.com.alelo.consumer.consumerpat.aplicacao.fakes;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;
import br.com.alelo.consumer.consumerpat.dominio.portas.GravadorDeCartao;

/**
 * A gravação do cartão em memória: substitui o cartão de mesmo número e tipo e anota a gravação
 * para o teste conferir.
 */
public interface GravacaoDeCartaoEmMemoria extends ComCartoesEmMemoria, GravadorDeCartao {

    @Override
    default Cartao salvar(Cartao cartao) {
        memoria().cartoes().put(new ChaveDeCartao(cartao.numero(), cartao.tipo()), cartao);
        memoria().gravacoes().add(cartao);
        return cartao;
    }
}
