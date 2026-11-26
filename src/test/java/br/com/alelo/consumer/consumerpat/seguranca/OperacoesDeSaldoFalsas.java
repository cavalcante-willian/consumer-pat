package br.com.alelo.consumer.consumerpat.seguranca;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.medicao.CartaoDeDisputa;
import br.com.alelo.consumer.consumerpat.medicao.OperacoesDoAlvo;
import br.com.alelo.consumer.consumerpat.medicao.Pedido;
import br.com.alelo.consumer.consumerpat.medicao.RespostaHttp;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * As operações de dinheiro de um sistema de mentira, para provar as verificações de saldo.
 *
 * <p>
 * Guarda um saldo só. O sistema que se defende recusa o valor que não é positivo nem cabe em
 * dinheiro, e o que não se defende subtrai o que vier, inclusive um valor negativo, que soma ao
 * saldo.
 */
final class OperacoesDeSaldoFalsas implements OperacoesDoAlvo {

    private static final BigDecimal TETO = new BigDecimal("1000000000000");

    private final boolean seDefende;

    private BigDecimal saldo = BigDecimal.ZERO;

    OperacoesDeSaldoFalsas(boolean seDefende) {
        this.seDefende = seDefende;
    }

    BigDecimal saldo() {
        return saldo;
    }

    void definirSaldo(BigDecimal novo) {
        this.saldo = novo;
    }

    @Override
    public String nome() {
        return "falso";
    }

    @Override
    public CartaoDeDisputa prepararFarmaciaComSaldo(String valor) {
        this.saldo = new BigDecimal(valor);
        return new CartaoDeDisputa("1", "1");
    }

    @Override
    public int comprarNaFarmacia(CartaoDeDisputa cartao, String valor) {
        var compra = new BigDecimal(valor);
        var aceita = !seDefende || compra.signum() > 0 && compra.compareTo(TETO) < 0;
        this.saldo = Map.of(true, saldo.subtract(compra), false, saldo).get(aceita);
        return Map.of(true, 201, false, 400).get(aceita);
    }

    @Override
    public BigDecimal saldoDe(CartaoDeDisputa cartao) {
        return saldo;
    }

    @Override
    public RespostaHttp cadastrar(int indice) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Pedido> listagens(int total) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Pedido> consultas(int total) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RespostaHttp creditar(TipoCartao tipo, int indice, String valor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RespostaHttp comprar(TipoCartao tipo, int indice, String valor) {
        throw new UnsupportedOperationException();
    }
}
