package br.com.alelo.consumer.consumerpat.aplicacao.fakes;

import br.com.alelo.consumer.consumerpat.dominio.log.CampoDeLog;
import br.com.alelo.consumer.consumerpat.dominio.log.NivelDeLog;
import br.com.alelo.consumer.consumerpat.dominio.log.SaidaDeLogPorta;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A porta de log em memória: guarda cada evento com o nível, a causa e os campos.
 *
 * <p>
 * É assim que um teste prova que um caso de uso registrou o fato certo, e que o log não contém o
 * número do cartão, sem mexer no Logback. A lista é segura para várias threads, porque o teste de
 * concorrência registra de várias ao mesmo tempo.
 */
public class SaidaDeLogFake implements SaidaDeLogPorta {

    /**
     * Um evento registrado.
     *
     * @param nivel  o nível
     * @param evento o nome do evento
     * @param causa  a causa, se houve
     * @param campos os campos, na ordem em que vieram
     */
    public record Evento(NivelDeLog nivel, String evento, Optional<Throwable> causa,
            List<CampoDeLog> campos) {

        /**
         * Lê o valor de um campo pelo nome.
         *
         * @param nome o nome do campo
         * @return o valor, ou vazio se o evento não tem o campo ou se ele é nulo
         */
        public Optional<Object> valorDe(String nome) {
            return campos.stream().filter(campo -> campo.nome().equals(nome)).findFirst()
                    .map(CampoDeLog::valor);
        }

        /**
         * Junta o evento e todos os campos num texto, para provar o que ele não contém.
         *
         * @return o evento e os campos, como {@code evento nome=valor}
         */
        public String texto() {
            return evento + " " + campos;
        }
    }

    private final List<Evento> eventos = new CopyOnWriteArrayList<>();

    @Override
    public void registrar(NivelDeLog nivel, String evento, Optional<Throwable> causa,
            CampoDeLog... campos) {
        eventos.add(new Evento(nivel, evento, causa, List.copyOf(Arrays.asList(campos))));
    }

    /**
     * Os eventos registrados, na ordem.
     *
     * @return uma cópia da lista
     */
    public List<Evento> eventos() {
        return List.copyOf(eventos);
    }
}
