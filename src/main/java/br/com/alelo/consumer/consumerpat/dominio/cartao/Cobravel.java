package br.com.alelo.consumer.consumerpat.dominio.cartao;

import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;

import java.math.BigDecimal;

/**
 * Papel de um tipo de cartão que sabe calcular o valor que cobra por uma compra.
 */
public interface Cobravel extends ComAjusteDoValor {

    /**
     * O valor efetivamente cobrado: o valor informado vezes um mais o ajuste do tipo.
     *
     * <p>
     * Guarda a fração de centavo que o cálculo produz: 10% de desconto sobre 33,33 é 29,997, sem
     * arredondar para centavos. A multiplicação vem antes de qualquer divisão, e nenhuma delas
     * passa por ponto flutuante.
     *
     * @param valorInformado o valor da compra, como informado
     * @return o valor que sai do cartão
     */
    default Dinheiro valorCobrado(Dinheiro valorInformado) {
        return valorInformado.vezes(BigDecimal.ONE.add(ajusteDoValor()));
    }
}
