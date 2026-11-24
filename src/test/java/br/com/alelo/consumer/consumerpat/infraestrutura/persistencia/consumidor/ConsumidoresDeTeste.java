package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Monta a linha de um consumidor para os testes de persistência, ainda não gravada.
 *
 * <p>
 * Todo cartão tem dono, e quase todo teste de persistência do cartão precisa de um consumidor que
 * não é o assunto dele. Cada linha sai com um documento diferente, porque o documento é único, e um
 * teste que grava dois consumidores não colide por acidente.
 */
public final class ConsumidoresDeTeste {

    private static final AtomicLong SEQUENCIA = new AtomicLong(10_000_000_000L);

    private ConsumidoresDeTeste() {
    }

    /**
     * Uma linha de consumidor com dados válidos e um documento que ninguém usou ainda.
     *
     * @return a linha, ainda não gravada
     */
    public static ConsumidorEntidade linha() {
        return linhaComDocumento(Long.toString(SEQUENCIA.incrementAndGet()));
    }

    /**
     * Uma linha de consumidor com dados válidos e o documento informado.
     *
     * @param documento os dígitos do documento
     * @return a linha, ainda não gravada
     */
    public static ConsumidorEntidade linhaComDocumento(String documento) {
        var entidade = new ConsumidorEntidade();
        entidade.setNome("Maria Souza");
        entidade.setDocumento(documento);
        entidade.setDataNascimento(LocalDate.of(1990, 5, 17));
        entidade.setCelular("11999998888");
        entidade.setEmail("maria@exemplo.com.br");
        entidade.setRua("Rua das Flores");
        entidade.setNumero("12-B");
        entidade.setCidade("São Paulo");
        entidade.setPais("Brasil");
        entidade.setCep("01001000");
        return entidade;
    }
}
