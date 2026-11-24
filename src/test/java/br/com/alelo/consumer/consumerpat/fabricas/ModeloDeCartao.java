package br.com.alelo.consumer.consumerpat.fabricas;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;

/**
 * O que o teste quer de um cartão: o tipo, o número e o saldo, antes de virar o cartão do domínio.
 *
 * <p>
 * É imutável, e trocar um dado devolve outro modelo. Só guarda o dado e declara os papéis: trocar o
 * saldo, trocar o número e construir o cartão são cada um o seu arquivo.
 *
 * @param tipo   o tipo do cartão
 * @param numero o número, só dígitos
 * @param saldo  o saldo, como decimal com ponto
 */
public record ModeloDeCartao(TipoCartao tipo, String numero, String saldo)
        implements ComSaldoTrocavel, ComNumeroTrocavel, CartaoConstruivel {
}
