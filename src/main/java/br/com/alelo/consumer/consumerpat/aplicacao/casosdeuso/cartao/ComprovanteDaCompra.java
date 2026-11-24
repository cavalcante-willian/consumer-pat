package br.com.alelo.consumer.consumerpat.aplicacao.casosdeuso.cartao;

import br.com.alelo.consumer.consumerpat.aplicacao.dtos.DtoComprovanteCompra;
import br.com.alelo.consumer.consumerpat.dominio.cartao.Cartao;
import br.com.alelo.consumer.consumerpat.dominio.extrato.Extrato;

/**
 * Monta o comprovante de uma compra efetivada.
 *
 * <p>
 * O comprovante é o extrato gravado mais o saldo que sobrou, e é tirado do próprio extrato, e não
 * do pedido: assim o que o cliente vê é exatamente o que foi registrado, sem uma segunda cópia dos
 * valores que pudesse divergir. É a única peça que sabe como o resultado vira resposta.
 */
public final class ComprovanteDaCompra {

    private ComprovanteDaCompra() {
    }

    /**
     * Monta o comprovante.
     *
     * @param extrato  o extrato da compra, já registrado
     * @param debitado o cartão depois do débito, de onde vem o saldo restante
     * @return o comprovante
     */
    public static DtoComprovanteCompra montar(Extrato extrato, Cartao debitado) {
        return new DtoComprovanteCompra(extrato.numeroDoCartao(), extrato.tipo(),
                extrato.valorInformado(), extrato.valorCobrado(), debitado.saldo(),
                extrato.dataDaCompra());
    }
}
