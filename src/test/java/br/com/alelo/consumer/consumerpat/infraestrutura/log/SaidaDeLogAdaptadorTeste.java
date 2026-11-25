package br.com.alelo.consumer.consumerpat.infraestrutura.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static br.com.alelo.consumer.consumerpat.dominio.log.CampoDeLog.campo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova o adaptador contra o Logback de verdade, com um {@code ListAppender} no logger do projeto.
 */
class SaidaDeLogAdaptadorTeste {

    private final Logger logger = (Logger) LoggerFactory
            .getLogger(SaidaDeLogAdaptador.NOME_DO_LOGGER);

    private final ListAppender<ILoggingEvent> eventos = new ListAppender<>();

    private final SaidaDeLogAdaptador saida = new SaidaDeLogAdaptador();

    private Level nivelAnterior;

    @BeforeEach
    void escutar() {
        nivelAnterior = logger.getLevel();
        logger.setLevel(Level.TRACE);
        eventos.start();
        logger.addAppender(eventos);
    }

    @AfterEach
    void parar() {
        logger.detachAppender(eventos);
        logger.setLevel(nivelAnterior);
    }

    private String linha() {
        return eventos.list.get(0).getFormattedMessage();
    }

    @Test
    void deve_formatar_evento_e_campos_quando_registrar() {
        // Agir
        saida.informacao("compra.registrada", campo("consumidorId", 42),
                campo("tipoCartao", "ALIMENTACAO"), campo("valorCobrado", "29.997000"));

        // Verificar
        assertThat(linha()).isEqualTo(
                "compra.registrada consumidorId=42 tipoCartao=ALIMENTACAO valorCobrado=29.997000");
        assertThat(eventos.list.get(0).getLevel()).isEqualTo(Level.INFO);
    }

    @Test
    void deve_registrar_so_o_evento_quando_nao_ha_campos() {
        // Agir
        saida.informacao("consumidor.criado");

        // Verificar
        assertThat(linha()).isEqualTo("consumidor.criado");
    }

    @Test
    void deve_mascarar_o_valor_quando_o_nome_do_campo_e_sensivel() {
        // Agir
        saida.informacao("cartao.creditado", campo("numeroCartao", "5555000011112222"),
                campo("documento", "12345678901"), campo("celular", "11999998888"),
                campo("email", "maria@exemplo.com.br"), campo("telefone", "1133334444"),
                campo("senha", "segredo123"), campo("token", "abc"), campo("segredo", "x"));

        // Verificar
        assertThat(linha()).isEqualTo("cartao.creditado numeroCartao=*** documento=*** celular=*** "
                + "email=*** telefone=*** senha=*** token=*** segredo=***");
    }

    @Test
    void deve_mascarar_quando_o_nome_so_contem_o_sensivel_ou_muda_a_caixa() {
        // Agir
        saida.informacao("consumidor.atualizado", campo("telefoneResidencial", "1133334444"),
                campo("NumeroCartaoOrigem", "4111111111111111"), campo("EMAIL", "a@b.c"));

        // Verificar
        assertThat(linha()).isEqualTo("consumidor.atualizado telefoneResidencial=*** "
                + "NumeroCartaoOrigem=*** EMAIL=***");
    }

    @Test
    void deve_manter_o_valor_quando_o_nome_nao_e_sensivel() {
        // Agir
        saida.informacao("consumidor.criado", campo("consumidorId", 7), campo("versao", 0));

        // Verificar
        assertThat(linha()).isEqualTo("consumidor.criado consumidorId=7 versao=0");
    }

    @Test
    void deve_trocar_as_quebras_de_linha_quando_o_valor_tenta_forjar_outra_linha() {
        // Agir
        saida.informacao("requisicao.concluida",
                campo("caminho", "/api\n2026-01-01T00:00:00Z  INFO [x] forjada - ok\r"));

        // Verificar: uma linha só, e a forjada fica como texto do campo
        assertThat(linha()).doesNotContain("\n").doesNotContain("\r");
        assertThat(linha()).startsWith("requisicao.concluida caminho=/api_2026");
    }

    @Test
    void deve_imprimir_nulo_quando_o_valor_do_campo_e_nulo() {
        // Agir
        saida.aviso("lock.timeout", campo("operacao", null));

        // Verificar
        assertThat(linha()).isEqualTo("lock.timeout operacao=null");
    }

    @Test
    void deve_usar_o_nivel_do_logback_correspondente_quando_registrar_cada_nivel() {
        // Agir
        saida.depuracao("a.feita");
        saida.informacao("b.feita");
        saida.aviso("c.feita");
        saida.erro("d.feita");
        saida.critico("e.feita");

        // Verificar
        assertThat(eventos.list).extracting(ILoggingEvent::getLevel).containsExactly(Level.DEBUG,
                Level.INFO, Level.WARN, Level.ERROR, Level.ERROR);
    }

    @Test
    void deve_marcar_como_critico_quando_registrar_critico() {
        // Agir
        saida.critico("banco.indisponivel");
        saida.erro("falha.inesperada");

        // Verificar: só o crítico leva o marcador, e é ele que permite rotear ou alertar
        assertThat(eventos.list.get(0).getMarker().getName())
                .isEqualTo(SaidaDeLogAdaptador.NOME_DO_MARCADOR_CRITICO);
        assertThat(eventos.list.get(1).getMarker()).isNull();
    }

    @Test
    void deve_levar_a_causa_ao_logback_quando_registrar_erro_com_causa() {
        // Preparar
        var causa = new IllegalStateException("falhou");

        // Agir
        saida.erroComCausa("falha.inesperada", causa, campo("operacao", "criar"));

        // Verificar
        assertThat(eventos.list.get(0).getThrowableProxy().getMessage()).isEqualTo("falhou");
        assertThat(linha()).isEqualTo("falha.inesperada operacao=criar");
    }

    @Test
    void deve_registrar_sem_causa_quando_o_nivel_nao_tem_causa() {
        // Agir
        saida.erro("falha.inesperada");

        // Verificar
        assertThat(eventos.list.get(0).getThrowableProxy()).isNull();
    }

    @Test
    void deve_registrar_no_logger_do_projeto_quando_registrar() {
        // Agir
        saida.informacao("a.feita");

        // Verificar
        assertThat(eventos.list.get(0).getLoggerName())
                .isEqualTo("br.com.alelo.consumer.consumerpat.eventos");
    }
}
