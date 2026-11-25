package br.com.alelo.consumer.consumerpat.fabricas;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Gera um documento de onze dígitos que nenhum outro teste usa.
 *
 * <p>
 * O banco em memória é um só para todos os testes da mesma execução, e o documento do consumidor é
 * único. Um teste que cadastra consumidores pela API, e que não pode desfazer o que gravou porque
 * as threads usam transações próprias, precisa de um documento que ninguém tenha usado. Os
 * documentos fixos dos outros testes começam com outros dígitos, e a sequência daqui começa em
 * {@value #BASE}.
 */
public final class DocumentosDeTeste {

    private static final long BASE = 70_000_000_000L;

    private static final AtomicLong SEQUENCIA = new AtomicLong();

    private DocumentosDeTeste() {
    }

    /**
     * O próximo documento, nunca devolvido antes nesta execução.
     *
     * @return onze dígitos, sem pontuação
     */
    public static String proximo() {
        return Long.toString(BASE + SEQUENCIA.incrementAndGet());
    }
}
