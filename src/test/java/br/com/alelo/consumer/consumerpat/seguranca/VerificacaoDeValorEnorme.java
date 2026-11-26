package br.com.alelo.consumer.consumerpat.seguranca;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Compra de um valor que não cabe em dinheiro algum: o sistema tem de recusar, e o saldo tem de
 * ficar onde estava.
 */
public final class VerificacaoDeValorEnorme implements Verificacao {

    private static final BigDecimal SALDO_INICIAL = new BigDecimal("100.00");

    private static final Map<Boolean, String> SITUACAO_DO_SALDO = Map.of(true, "intacto", false,
            "alterado");

    @Override
    public String chave() {
        return "valor-enorme";
    }

    @Override
    public Achado verificar(SondaDeSeguranca sonda) throws Exception {
        var operacoes = sonda.operacoes();
        var cartao = operacoes.prepararFarmaciaComSaldo(SALDO_INICIAL.toPlainString());
        var status = operacoes.comprarNaFarmacia(cartao, "1e308");
        var intacto = operacoes.saldoDe(cartao).compareTo(SALDO_INICIAL) == 0;
        return new Achado(chave(), "compra de um valor enorme (1e308)",
                Status.erroDeCliente(status) && intacto,
                "status " + status + ", saldo " + SITUACAO_DO_SALDO.get(intacto));
    }
}
