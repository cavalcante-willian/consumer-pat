package br.com.alelo.consumer.consumerpat.aplicacao.fakes;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Um gerenciador de transação que só registra o que lhe pedem, sem banco.
 *
 * <p>
 * Serve para provar, num contexto Spring de verdade, que o proxy de um caso de uso pede a transação
 * que a anotação declara, e com a definição certa (por exemplo, só de leitura).
 */
public class GerenciadorDeTransacaoFake implements PlatformTransactionManager {

    private final List<TransactionDefinition> abertas = new ArrayList<>();

    private int confirmadas;

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definicao) {
        abertas.add(definicao);
        return new SimpleTransactionStatus();
    }

    @Override
    public void commit(TransactionStatus status) {
        confirmadas++;
    }

    @Override
    public void rollback(TransactionStatus status) {
        // Nenhum teste que o usa espera desfazer: a transação só é registrada.
    }

    /**
     * As definições das transações abertas, na ordem.
     *
     * @return cópia da lista
     */
    public List<TransactionDefinition> abertas() {
        return List.copyOf(abertas);
    }

    /**
     * Quantas transações foram confirmadas.
     *
     * @return a contagem
     */
    public int confirmadas() {
        return confirmadas;
    }
}
