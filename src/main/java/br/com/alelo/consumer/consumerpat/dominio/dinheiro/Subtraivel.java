package br.com.alelo.consumer.consumerpat.dominio.dinheiro;

/**
 * Papel de uma quantia que sabe subtrair outra.
 */
public interface Subtraivel extends ComQuantia {

    /**
     * Subtração exata. O resultado pode ser negativo: este papel não decide se saldo negativo é
     * permitido, isso é regra de quem o usa.
     *
     * @param outro a quantia a subtrair
     * @return a diferença, na mesma escala
     */
    default Dinheiro menos(Dinheiro outro) {
        return new Dinheiro(valor().subtract(outro.valor()));
    }
}
