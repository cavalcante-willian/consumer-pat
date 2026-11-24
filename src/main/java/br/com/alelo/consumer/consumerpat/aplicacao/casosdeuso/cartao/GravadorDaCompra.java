package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao;

import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;
import br.com.alelo.consumer.consumerpat.dominio.extrato.Extrato;
import br.com.alelo.consumer.consumerpat.dominio.portas.CartaoPorta;
import br.com.alelo.consumer.consumerpat.dominio.portas.ExtratoPorta;

/**
 * Grava o que uma compra efetivada deixa para trás: o cartão com o novo saldo e o extrato.
 *
 * <p>
 * É a única peça que sabe o que se grava numa compra, e por isso é onde muda se a compra passar a
 * gravar algo mais. Não abre transação: roda dentro da do caso de uso, de modo que o cartão e o
 * extrato são gravados juntos ou nenhum dos dois.
 */
public class GravadorDaCompra {

    private final CartaoPorta cartaoPorta;

    private final ExtratoPorta extratoPorta;

    /**
     * Cria o gravador.
     *
     * @param cartaoPorta  onde o cartão é gravado
     * @param extratoPorta onde o extrato é registrado
     */
    public GravadorDaCompra(CartaoPorta cartaoPorta, ExtratoPorta extratoPorta) {
        this.cartaoPorta = cartaoPorta;
        this.extratoPorta = extratoPorta;
    }

    /**
     * Grava o cartão debitado e o extrato da compra.
     *
     * @param debitado o cartão com o saldo já debitado
     * @param extrato  o extrato da compra
     * @return o cartão como ficou gravado
     */
    public Cartao gravar(Cartao debitado, Extrato extrato) {
        extratoPorta.registrar(extrato);
        return cartaoPorta.salvar(debitado);
    }
}
