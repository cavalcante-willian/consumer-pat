package br.com.alelo.consumer.consumerpat.dominio.log;

/**
 * A porta do log: o vocabulário único em português por onde todo o projeto registra um evento.
 *
 * <p>
 * Só compõe os papéis, e cada um tem o seu arquivo. Só {@link RegistradorDeLog#registrar} é
 * implementado por quem fala com uma biblioteca de log, e por isso o domínio e a aplicação não
 * conhecem SLF4J nem Logback: veem só esta porta. O evento é um nome estável
 * ({@code substantivo.particípio}), e os dados vão em {@link CampoDeLog} nomeados, nunca
 * concatenados na mensagem.
 */
public interface SaidaDeLogPorta extends DepuracaoDeLog, InformacaoDeLog, AvisoDeLog, ErroDeLog,
        ErroComCausaDeLog, CriticoDeLog {
}
