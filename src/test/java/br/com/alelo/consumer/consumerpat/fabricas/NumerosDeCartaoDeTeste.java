package br.com.alelo.consumer.consumerpat.fabricas;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Gera um número de cartão de dezesseis dígitos que nenhum outro teste usa.
 *
 * <p>
 * É o mesmo problema dos documentos: o banco é um só para a execução inteira e o número do cartão é
 * único. Os números fixos dos outros testes ({@link FabricaDeCartoes#NUMERO_PADRAO} e os dos testes
 * de ponta a ponta) começam com outros dígitos, e a sequência daqui começa em {@value #BASE}.
 */
public final class NumerosDeCartaoDeTeste {

    private static final long BASE = 7_000_000_000_000_000L;

    private static final AtomicLong SEQUENCIA = new AtomicLong();

    private NumerosDeCartaoDeTeste() {
    }

    /**
     * O próximo número, nunca devolvido antes nesta execução.
     *
     * @return dezesseis dígitos, sem separador
     */
    public static String proximo() {
        return Long.toString(BASE + SEQUENCIA.incrementAndGet());
    }
}
