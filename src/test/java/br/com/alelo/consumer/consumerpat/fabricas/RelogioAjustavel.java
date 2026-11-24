package br.com.alelo.consumer.consumerpat.fabricas;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Um relógio que o teste controla: parte de um instante fixo e só anda quando o teste manda.
 *
 * <p>
 * Existe para provar o que depende do tempo, como a data de atualização que precisa avançar, sem
 * dormir e sem depender do relógio da máquina.
 */
public final class RelogioAjustavel extends Clock {

    private Instant instante;

    /**
     * Cria o relógio parado num instante.
     *
     * @param inicial o instante em que o relógio começa
     */
    public RelogioAjustavel(Instant inicial) {
        this.instante = inicial;
    }

    /**
     * Anda o relógio para a frente.
     *
     * @param duracao quanto andar
     */
    public void avancar(Duration duracao) {
        instante = instante.plus(duracao);
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zona) {
        return this;
    }

    @Override
    public Instant instant() {
        return instante;
    }
}
