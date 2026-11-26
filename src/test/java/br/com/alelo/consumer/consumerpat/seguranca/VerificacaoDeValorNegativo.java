package br.com.alelo.consumer.consumerpat.seguranca;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Compra de valor negativo: o sistema não pode aceitar e creditar o saldo por baixo dos panos.
 *
 * <p>
 * O legado subtrai o valor sem olhar o sinal, e uma compra de menos cinquenta soma cinquenta ao
 * saldo. É dinheiro criado por uma requisição que nenhum controle recusa.
 */
public final class VerificacaoDeValorNegativo implements Verificacao {

    private static final BigDecimal SALDO_INICIAL = new BigDecimal("100.00");

    @Override
    public String chave() {
        return "valor-negativo";
    }

    @Override
    public Achado verificar(SondaDeSeguranca sonda) throws Exception {
        var operacoes = sonda.operacoes();
        var cartao = operacoes.prepararFarmaciaComSaldo(SALDO_INICIAL.toPlainString());
        var status = operacoes.comprarNaFarmacia(cartao, "-50.00");
        var saldo = operacoes.saldoDe(cartao).setScale(2, RoundingMode.HALF_EVEN);
        return new Achado(chave(), "compra de valor negativo (-50,00) num cartão de 100,00",
                saldo.compareTo(SALDO_INICIAL) <= 0,
                "status " + status + ", saldo de 100,00 para " + saldo.toPlainString());
    }
}
