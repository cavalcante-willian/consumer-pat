package br.com.alelo.consumer.consumerpat.seguranca;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Atualizar o cadastro mandando o saldo junto: o saldo só muda por crédito e por compra, nunca por
 * uma atualização de dados.
 *
 * <p>
 * O legado grava o objeto recebido inteiro, e o saldo que veio no corpo vira o saldo do cartão. É a
 * regra "não deve ser possível alterar o saldo do cartão" do enunciado do desafio.
 */
public final class VerificacaoDeSaldoPorAtualizacao implements Verificacao {

    private static final BigDecimal SALDO_INICIAL = new BigDecimal("100.00");

    @Override
    public String chave() {
        return "saldo-por-atualizacao";
    }

    @Override
    public Achado verificar(SondaDeSeguranca sonda) throws Exception {
        var operacoes = sonda.operacoes();
        var cartao = operacoes.prepararFarmaciaComSaldo(SALDO_INICIAL.toPlainString());
        var resposta = sonda.atualizarMandandoSaldo(cartao, "999999.00");
        var saldo = operacoes.saldoDe(cartao).setScale(2, RoundingMode.HALF_EVEN);
        return new Achado(chave(), "atualizar o cadastro mandando um saldo de 999.999,00",
                saldo.compareTo(SALDO_INICIAL) <= 0,
                "status " + resposta.status() + ", saldo de 100,00 para " + saldo.toPlainString());
    }
}
