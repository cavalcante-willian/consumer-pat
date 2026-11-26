package br.com.alelo.consumer.consumerpat.medicao;

import java.util.List;

/**
 * Mede a consulta de um consumidor pelo id. O legado não tem a operação, e o relatório o mostra sem
 * medida, em vez de inventar uma.
 */
public final class CenarioDeConsultaPorId implements Cenario {

    private final int total;

    /**
     * Cria o cenário sobre uma base.
     *
     * @param total quantos consumidores a base tem
     */
    public CenarioDeConsultaPorId(int total) {
        this.total = total;
    }

    @Override
    public List<Medida> medir(OperacoesDoAlvo alvo) throws Exception {
        return MedicaoDePedidos.medir(alvo.consultas(total));
    }
}
