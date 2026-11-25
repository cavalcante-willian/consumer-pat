package br.com.alelo.consumer.consumerpat.infraestrutura.log;

import br.com.alelo.consumer.consumerpat.dominio.log.CampoDeLog;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Monta a linha do evento: {@code evento chave=valor chave=valor}.
 *
 * <p>
 * O SLF4J 1.7 do Spring Boot 2.7 não tem campos estruturados, e a convenção {@code chave=valor} é o
 * que permite filtrar e contar com uma ferramenta de texto. Sem campos, a linha é só o evento.
 */
final class MensagemDeLog {

    private MensagemDeLog() {
    }

    /**
     * Monta a mensagem.
     *
     * @param evento o nome estável do evento
     * @param campos os dados, cada um com o seu nome
     * @return a mensagem, numa linha só e com o dado sensível mascarado
     */
    static String montar(String evento, CampoDeLog... campos) {
        return Stream
                .concat(Stream.of(TextoDeUmaLinha.de(evento)),
                        Arrays.stream(campos).map(
                                campo -> campo.nome() + "=" + ValorDeCampoDeLog.paraTexto(campo)))
                .collect(Collectors.joining(" "));
    }
}
