package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeCartoes;

import java.math.BigDecimal;

/**
 * Monta a linha de um cartão para os testes de persistência, ainda não gravada.
 *
 * <p>
 * O saldo é texto, como o teste o escreve, e o número padrão é o da fábrica de cartões.
 */
final class CartoesDeTeste {

    private CartoesDeTeste() {
    }

    /**
     * Monta a linha com o número padrão.
     *
     * @param tipo  o tipo do cartão
     * @param saldo o saldo, como decimal com ponto
     * @return a linha, ainda não gravada
     */
    static CartaoEntidade linha(TipoCartao tipo, String saldo) {
        return linha(tipo, FabricaDeCartoes.NUMERO_PADRAO, saldo);
    }

    /**
     * Monta a linha com o número informado.
     *
     * @param tipo   o tipo do cartão
     * @param numero o número, só dígitos
     * @param saldo  o saldo, como decimal com ponto
     * @return a linha, ainda não gravada
     */
    static CartaoEntidade linha(TipoCartao tipo, String numero, String saldo) {
        var entidade = new CartaoEntidade();
        entidade.setTipo(tipo);
        entidade.setNumero(numero);
        entidade.setSaldo(new BigDecimal(saldo));
        return entidade;
    }
}
