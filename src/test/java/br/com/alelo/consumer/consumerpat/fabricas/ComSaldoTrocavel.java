package br.com.alelo.consumer.consumerpat.fabricas;

/**
 * Papel de um modelo de cartão cujo saldo o teste pode trocar.
 */
public interface ComSaldoTrocavel extends ComDadosDeCartao {

    /**
     * Troca o saldo.
     *
     * @param saldo o saldo, como decimal com ponto
     * @return um novo modelo com o saldo informado; este não é alterado
     */
    default ModeloDeCartao comSaldo(String saldo) {
        return new ModeloDeCartao(tipo(), numero(), saldo);
    }
}
