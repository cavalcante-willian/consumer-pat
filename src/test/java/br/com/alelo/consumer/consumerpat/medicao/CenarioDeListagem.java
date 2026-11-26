package br.com.alelo.consumer.consumerpat.medicao;

import java.util.List;

/**
 * Mede a listagem: o legado devolve a tabela inteira numa resposta só, e o novo uma página por vez.
 */
public final class CenarioDeListagem implements Cenario {

    private final int total;

    /**
     * Cria o cenário sobre uma base.
     *
     * @param total quantos consumidores a base tem
     */
    public CenarioDeListagem(int total) {
        this.total = total;
    }

    @Override
    public List<Medida> medir(OperacoesDoAlvo alvo) throws Exception {
        return MedicaoDePedidos.medir(alvo.listagens(total));
    }
}
