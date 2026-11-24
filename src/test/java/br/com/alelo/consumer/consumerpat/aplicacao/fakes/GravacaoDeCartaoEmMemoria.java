package br.com.alelo.consumer.consumerpat.aplicacao.fakes;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;
import br.com.alelo.consumer.consumerpat.dominio.portas.GravadorDeCartao;

import java.util.Optional;

/**
 * A gravação do cartão em memória, com o contrato da porta real: substitui o cartão que já existe
 * com o mesmo número e tipo, anota a gravação para o teste conferir, e lança se o cartão não
 * existe, porque gravar um cartão que ninguém leu é bug de quem chamou.
 */
public interface GravacaoDeCartaoEmMemoria extends ComCartoesEmMemoria, GravadorDeCartao {

    @Override
    default Cartao salvar(Cartao cartao) {
        Optional.ofNullable(memoria().cartoes()
                .replace(new ChaveDeCartao(cartao.numero(), cartao.tipo()), cartao))
                .orElseThrow(() -> new IllegalStateException(
                        "Não existe cartão para gravar: " + cartao.numero()));
        memoria().gravacoes().add(cartao);
        return cartao;
    }
}
