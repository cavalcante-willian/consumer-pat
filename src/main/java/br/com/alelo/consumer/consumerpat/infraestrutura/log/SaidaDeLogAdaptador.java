package br.com.alelo.consumer.consumerpat.infraestrutura.log;

import br.com.alelo.consumer.consumerpat.dominio.log.CampoDeLog;
import br.com.alelo.consumer.consumerpat.dominio.log.NivelDeLog;
import br.com.alelo.consumer.consumerpat.dominio.log.SaidaDeLogPorta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Cumpre a porta de log sobre o SLF4J: a única classe do projeto que fala com ele.
 *
 * <p>
 * Monta a linha {@code evento chave=valor}, mascara o dado sensível pelo nome do campo e entrega ao
 * Logback, que decide destino, formato e rotação por propriedade. O SLF4J não tem {@code FATAL}, e
 * o nível crítico vira {@code ERROR} com o marcador {@value #NOME_DO_MARCADOR_CRITICO}, o que
 * permite rotear ou alertar só o crítico. Todos os eventos saem por um logger só, e o nível por
 * pacote da configuração vale para ele pelo prefixo {@code br.com.alelo.consumer}.
 */
public class SaidaDeLogAdaptador implements SaidaDeLogPorta {

    /** O nome do logger de todos os eventos do projeto. */
    public static final String NOME_DO_LOGGER = "br.com.alelo.consumer.consumerpat.eventos";

    /** O nome do marcador que distingue o crítico do erro comum. */
    public static final String NOME_DO_MARCADOR_CRITICO = "CRITICO";

    private static final Logger LOGGER = LoggerFactory.getLogger(NOME_DO_LOGGER);

    private static final Marker CRITICO = MarkerFactory.getMarker(NOME_DO_MARCADOR_CRITICO);

    private static final Map<NivelDeLog, EscritaNoSlf4j> ESCRITAS = new EnumMap<>(
            Map.of(NivelDeLog.DEPURACAO, (logger, mensagem, causa) -> logger.debug(mensagem, causa),
                    NivelDeLog.INFORMACAO,
                    (logger, mensagem, causa) -> logger.info(mensagem, causa), NivelDeLog.AVISO,
                    (logger, mensagem, causa) -> logger.warn(mensagem, causa), NivelDeLog.ERRO,
                    (logger, mensagem, causa) -> logger.error(mensagem, causa), NivelDeLog.CRITICO,
                    (logger, mensagem, causa) -> logger.error(CRITICO, mensagem, causa)));

    @Override
    public void registrar(NivelDeLog nivel, String evento, Optional<Throwable> causa,
            CampoDeLog... campos) {
        ESCRITAS.get(nivel).escrever(LOGGER, MensagemDeLog.montar(evento, campos),
                causa.orElse(null));
    }
}
