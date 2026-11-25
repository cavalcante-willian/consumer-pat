package br.com.alelo.consumer.consumerpat.dominio.log;

import java.util.Optional;

/**
 * Operação-base do log: registra um evento, num nível, com os seus campos.
 *
 * <p>
 * É a única operação que um adaptador ou um fake implementa. Os seis papéis de nível
 * ({@link InformacaoDeLog}, {@link AvisoDeLog} e os demais) são escritos em cima dela, cada um em
 * seu arquivo, e a {@link SaidaDeLogPorta} só os compõe.
 */
public interface RegistradorDeLog {

    /**
     * Registra o evento.
     *
     * @param nivel  o nível do evento
     * @param evento o nome estável do evento, como {@code compra.registrada}
     * @param causa  a causa, quando ela não carrega dado sensível
     * @param campos os dados do evento, cada um com o seu nome
     */
    void registrar(NivelDeLog nivel, String evento, Optional<Throwable> causa,
            CampoDeLog... campos);
}
