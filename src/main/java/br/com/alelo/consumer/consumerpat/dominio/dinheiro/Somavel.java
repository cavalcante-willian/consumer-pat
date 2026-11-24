package br.com.alelo.consumer.consumerpat.dominio.dinheiro;

/**
 * Papel de uma quantia que sabe somar-se a outra.
 */
public interface Somavel extends ComQuantia {

    /**
     * Soma exata: as duas quantias têm a mesma escala, então a soma nunca
     * arredonda.
     *
     * @param outro a quantia a somar
     * @return a soma, na mesma escala
     */
    default Dinheiro mais(Dinheiro outro) {
        return new Dinheiro(valor().add(outro.valor()));
    }
}
