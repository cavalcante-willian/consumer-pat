package br.com.alelo.consumer.consumerpat.fabricas;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;

import java.math.BigDecimal;

/**
 * Papel de um modelo de cartão que sabe virar o cartão do domínio.
 */
public interface CartaoConstruivel extends ComDadosDeCartao {

    /**
     * Constrói o cartão.
     *
     * @return o cartão com o tipo, o número e o saldo do modelo
     * @throws IllegalArgumentException se o número ou o saldo do modelo não formam um cartão válido
     */
    default Cartao construir() {
        return new Cartao(new NumeroDeCartao(numero()), tipo(),
                new Dinheiro(new BigDecimal(saldo())));
    }
}
