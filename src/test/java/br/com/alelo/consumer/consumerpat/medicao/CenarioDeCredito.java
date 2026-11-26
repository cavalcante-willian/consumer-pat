package br.com.alelo.consumer.consumerpat.medicao;

import java.util.List;

/**
 * Mede o crédito de cada tipo de cartão, no início, no meio e no fim da base. Roda antes das
 * compras, porque no sistema novo o saldo começa em zero e é o crédito que o move.
 */
public final class CenarioDeCredito implements Cenario {

    private final int total;

    /**
     * Cria o cenário sobre uma base.
     *
     * @param total quantos consumidores a base tem
     */
    public CenarioDeCredito(int total) {
        this.total = total;
    }

    @Override
    public List<Medida> medir(OperacoesDoAlvo alvo) throws Exception {
        return MedicaoDePedidos.medir(alvo.creditos(total));
    }
}
