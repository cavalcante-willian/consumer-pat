package br.com.alelo.consumer.consumerpat.dominio.dinheiro;

/**
 * Papel de uma quantia que se ordena pelo valor.
 *
 * <p>
 * Compara pelo valor numérico, e não pela representação: como toda quantia tem a mesma escala, 2,0
 * e 2,00 já são iguais, e esta ordem concorda com {@code equals}.
 */
public interface ComparavelPorQuantia extends ComQuantia, Comparable<Dinheiro> {

    @Override
    default int compareTo(Dinheiro outro) {
        return valor().compareTo(outro.valor());
    }
}
