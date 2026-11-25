package br.com.alelo.consumer.consumerpat.api.saude;

import br.com.alelo.consumer.consumerpat.dominio.saude.EstadoDeSaude;

import java.util.stream.Collectors;

/**
 * Converte o estado de saúde do domínio na mensagem de saída.
 */
public final class SaudeEmMensagem {

    private SaudeEmMensagem() {
    }

    /**
     * Monta a mensagem.
     *
     * @param estado o estado de saúde
     * @return a situação geral e o estado de cada dependência, pelo nome dela
     */
    public static MensagemSaude montar(EstadoDeSaude estado) {
        return new MensagemSaude(estado.situacao().name(),
                estado.dependencias().entrySet().stream()
                        .collect(Collectors.toMap(dependencia -> dependencia.getKey().nome(),
                                dependencia -> dependencia.getValue().name())));
    }
}
