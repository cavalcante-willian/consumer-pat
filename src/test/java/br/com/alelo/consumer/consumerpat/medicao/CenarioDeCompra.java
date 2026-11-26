package br.com.alelo.consumer.consumerpat.medicao;

import java.util.List;

/**
 * Mede a compra em cada tipo de cartão, no início, no meio e no fim da base. Só faz sentido depois
 * do crédito, que dá saldo aos cartões do sistema novo.
 */
public final class CenarioDeCompra implements Cenario {

    private final int total;

    /**
     * Cria o cenário sobre uma base.
     *
     * @param total quantos consumidores a base tem
     */
    public CenarioDeCompra(int total) {
        this.total = total;
    }

    @Override
    public List<Medida> medir(OperacoesDoAlvo alvo) throws Exception {
        return MedicaoDePedidos.medir(alvo.compras(total));
    }
}
