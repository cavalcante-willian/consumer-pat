package br.com.alelo.consumer.consumerpat.dominio.erros.regradenegocio;

import br.com.alelo.consumer.consumerpat.dominio.dinheiro.Dinheiro;

/**
 * O saldo do cartão não cobre o valor que se tentou debitar.
 *
 * <p>
 * Carrega o saldo e o valor para quem registra o erro em log, mas a mensagem não os repete: ela vai
 * para quem fez o pedido, e o saldo de um cartão não é dado para devolver em uma resposta de erro.
 * Não carrega o número do cartão, porque o erro nasce dentro do cartão, que já é quem o identifica
 * no contexto do pedido.
 *
 * @param saldo o saldo que o cartão tinha
 * @param valor o valor que se tentou debitar
 */
public record ErroSaldoInsuficiente(Dinheiro saldo, Dinheiro valor) implements ErroRegraDeNegocio {

    /** Código estável desta variante. */
    public static final String CODIGO = "CPT-REG-CRT-001";

    @Override
    public String codigo() {
        return CODIGO;
    }

    @Override
    public String mensagem() {
        return "O saldo do cartão não cobre o valor da compra.";
    }
}
