package br.com.alelo.consumer.consumerpat.seguranca;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Comprar com um {@code GET}: uma leitura não pode mudar o saldo.
 *
 * <p>
 * Um {@code GET} pode ser repetido pelo navegador, guardado em cache, disparado por uma imagem numa
 * página de terceiros e registrado, com a query inteira, em cada log de acesso e de proxy. Quando
 * ele altera estado, tudo isso passa a mexer no dinheiro, e o número do cartão e o valor viajam na
 * URL.
 */
public final class VerificacaoDeEstadoPorGet implements Verificacao {

    private static final BigDecimal SALDO_INICIAL = new BigDecimal("100.00");

    @Override
    public String chave() {
        return "estado-alterado-por-get";
    }

    @Override
    public Achado verificar(SondaDeSeguranca sonda) throws Exception {
        var operacoes = sonda.operacoes();
        var cartao = operacoes.prepararFarmaciaComSaldo(SALDO_INICIAL.toPlainString());
        var resposta = sonda.comprarPorGet(cartao, "10.00");
        var saldo = operacoes.saldoDe(cartao).setScale(2, RoundingMode.HALF_EVEN);
        return new Achado(chave(), "compra de 10,00 feita por GET",
                saldo.compareTo(SALDO_INICIAL) == 0,
                "status " + resposta.status() + ", saldo de 100,00 para " + saldo.toPlainString());
    }
}
