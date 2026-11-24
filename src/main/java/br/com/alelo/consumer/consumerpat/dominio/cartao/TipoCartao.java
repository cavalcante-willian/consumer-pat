package br.com.alelo.consumer.consumerpat.dominio.cartao;

import java.math.BigDecimal;

/**
 * Os tipos de cartão de benefício, cada um com o tipo de estabelecimento em que vale e o ajuste que
 * aplica ao valor da compra.
 *
 * <p>
 * O valor de uma compra só pode ser debitado do cartão do tipo que corresponde ao estabelecimento:
 * a correspondência é o {@link #codigoDeEstabelecimento()}. O ajuste é dado do tipo, e não uma
 * decisão tomada por quem calcula: alimentação dá 10% de desconto, farmácia não altera o valor e
 * combustível acrescenta 35%. O cálculo do valor cobrado é o papel {@link Cobravel}, e a leitura do
 * código vindo de fora é {@link TipoCartaoDoEstabelecimento}.
 */
public enum TipoCartao implements Cobravel, ComCodigoDeEstabelecimento {

    /** Alimentação: desconto de 10% sobre o valor. */
    ALIMENTACAO(1, "-0.10"),

    /** Farmácia: o valor não é alterado. */
    FARMACIA(2, "0.00"),

    /** Posto de combustível: acréscimo de 35% sobre o valor. */
    COMBUSTIVEL(3, "0.35");

    private final int codigoDeEstabelecimento;

    private final BigDecimal ajusteDoValor;

    TipoCartao(int codigoDeEstabelecimento, String ajusteDoValor) {
        this.codigoDeEstabelecimento = codigoDeEstabelecimento;
        this.ajusteDoValor = new BigDecimal(ajusteDoValor);
    }

    @Override
    public int codigoDeEstabelecimento() {
        return codigoDeEstabelecimento;
    }

    @Override
    public BigDecimal ajusteDoValor() {
        return ajusteDoValor;
    }
}
