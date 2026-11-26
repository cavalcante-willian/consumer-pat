package br.com.alelo.consumer.consumerpat.api.comum.tratamento;

import br.com.alelo.consumer.consumerpat.api.comum.problema.MensagemProblema;
import br.com.alelo.consumer.consumerpat.aplicacao.fakes.SaidaDeLogFake;
import br.com.alelo.consumer.consumerpat.dominio.log.NivelDeLog;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercita as traduções que nenhum controller provoca sozinho: falhas de persistência e a falha
 * inesperada. As de entrada e de rota são cobertas pelos testes dos controllers.
 */
class FiltroDeProblemaDeApiTeste {

    private static final String ROTA = "/api/v1/compras";
    private static final MediaType PROBLEMA = MediaType.valueOf("application/problem+json");

    private final SaidaDeLogFake saida = new SaidaDeLogFake();
    private final FiltroDeProblemaDeApi filtro = new FiltroDeProblemaDeApi(saida);
    private final MockHttpServletRequest requisicao = new MockHttpServletRequest("POST", ROTA);

    private static MensagemProblema corpoDe(org.springframework.http.ResponseEntity<Object> r) {
        return (MensagemProblema) r.getBody();
    }

    @Test
    void deve_responder_409_de_recurso_em_uso_quando_a_trava_esgota() {
        // Agir
        var resposta = filtro.traduzirTravaEsgotada(new CannotAcquireLockException("trava"),
                requisicao);

        // Verificar
        assertThat(resposta.getStatusCodeValue()).isEqualTo(409);
        assertThat(resposta.getHeaders().getContentType()).isEqualTo(PROBLEMA);
        assertThat(corpoDe(resposta).type()).isEqualTo("urn:consumer-pat:erro:CPT-CNF-SIS-002");
        assertThat(corpoDe(resposta).instance()).isEqualTo(ROTA);
    }

    @Test
    void deve_registrar_lock_timeout_como_aviso_quando_a_trava_esgota() {
        // Agir
        filtro.traduzirTravaEsgotada(new CannotAcquireLockException("trava"), requisicao);

        // Verificar
        assertThat(saida.eventos()).singleElement().satisfies(evento -> {
            assertThat(evento.nivel()).isEqualTo(NivelDeLog.AVISO);
            assertThat(evento.evento()).isEqualTo("lock.timeout");
            assertThat(evento.valorDe("metodo")).contains("POST");
            assertThat(evento.valorDe("caminho")).contains(ROTA);
            assertThat(evento.valorDe("operacao")).isEmpty();
        });
    }

    @Test
    void deve_registrar_conflito_detectado_como_aviso_quando_a_versao_mudou() {
        // Agir
        filtro.traduzirConflitoDeVersao(new ObjectOptimisticLockingFailureException("Cartao", 1L),
                requisicao);

        // Verificar: a entidade, e nunca um valor
        assertThat(saida.eventos()).singleElement().satisfies(evento -> {
            assertThat(evento.nivel()).isEqualTo(NivelDeLog.AVISO);
            assertThat(evento.evento()).isEqualTo("conflito.detectado");
            assertThat(evento.valorDe("entidade")).contains("Cartao");
            assertThat(evento.valorDe("metodo")).contains("POST");
            assertThat(evento.valorDe("caminho")).contains(ROTA);
            assertThat(evento.valorDe("operacao")).isEmpty();
        });
    }

    @Test
    void deve_registrar_so_o_nome_da_restricao_quando_o_banco_recusa_na_gravacao() {
        // Preparar: a restrição traz o valor colado, como o H2 faz
        var causa = new ConstraintViolationException("violou", new SQLException("x"),
                "UK_CONSUMIDORES_DOCUMENTO_INDEX_5 ON PUBLIC.CONSUMIDORES(DOCUMENTO) VALUES "
                        + "('12345678901')");

        // Agir
        filtro.traduzirIntegridadeViolada(new DataIntegrityViolationException("x", causa),
                requisicao);

        // Verificar
        assertThat(saida.eventos()).singleElement().satisfies(evento -> {
            assertThat(evento.evento()).isEqualTo("conflito.detectado");
            assertThat(evento.valorDe("restricao")).contains("UK_CONSUMIDORES_DOCUMENTO_INDEX_5");
            assertThat(evento.valorDe("metodo")).contains("POST");
            assertThat(evento.valorDe("caminho")).contains(ROTA);
            assertThat(evento.valorDe("operacao")).isEmpty();
            assertThat(evento.texto()).doesNotContain("12345678901").doesNotContain("VALUES");
        });
    }

    @Test
    void deve_registrar_a_falha_inesperada_com_a_pilha_e_sem_a_mensagem() {
        // Agir
        filtro.traduzirFalhaInesperada(new IllegalStateException("senha=segredo jdbc:h2:mem:x"),
                requisicao);

        // Verificar
        assertThat(saida.eventos()).singleElement().satisfies(evento -> {
            assertThat(evento.nivel()).isEqualTo(NivelDeLog.ERRO);
            assertThat(evento.evento()).isEqualTo("falha.inesperada");
            assertThat(evento.valorDe("metodo")).contains("POST");
            assertThat(evento.valorDe("caminho")).contains(ROTA);
            assertThat(evento.valorDe("operacao")).isEmpty();
            assertThat(evento.causa()).hasValueSatisfying(causa -> {
                assertThat(causa.getMessage()).isEqualTo("java.lang.IllegalStateException");
                assertThat(causa.getStackTrace()).isNotEmpty();
            });
        });
    }

    @Test
    void deve_responder_409_de_conflito_quando_a_versao_do_cartao_mudou() {
        // Agir
        var resposta = filtro.traduzirConflitoDeVersao(
                new ObjectOptimisticLockingFailureException("Cartao", 1L), requisicao);

        // Verificar
        assertThat(resposta.getStatusCodeValue()).isEqualTo(409);
        assertThat(corpoDe(resposta).type()).isEqualTo("urn:consumer-pat:erro:CPT-CNF-SIS-001");
    }

    @Test
    void deve_responder_409_sem_vazar_a_restricao_quando_o_banco_recusa_na_gravacao() {
        // Agir
        var resposta = filtro.traduzirIntegridadeViolada(
                new DataIntegrityViolationException(
                        "Unique index violation: UK_CONSUMIDORES_DOCUMENTO value 12345678901"),
                requisicao);

        // Verificar
        assertThat(resposta.getStatusCodeValue()).isEqualTo(409);
        assertThat(corpoDe(resposta).type()).isEqualTo("urn:consumer-pat:erro:CPT-CNF-SIS-001");
        assertThat(corpoDe(resposta).toString()).doesNotContain("UK_CONSUMIDORES")
                .doesNotContain("12345678901");
    }

    @Test
    void deve_responder_400_apontando_o_parametro_quando_o_argumento_e_de_tipo_errado() {
        // Preparar
        var excecao = new MethodArgumentTypeMismatchException("abc", Long.class, "id", null,
                new NumberFormatException("segredo"));

        // Agir
        var resposta = filtro.traduzirArgumentoInvalido(excecao, requisicao);

        // Verificar
        assertThat(resposta.getStatusCodeValue()).isEqualTo(400);
        assertThat(corpoDe(resposta).errors()).containsOnlyKeys("id");
        assertThat(corpoDe(resposta).toString()).doesNotContain("abc").doesNotContain("segredo")
                .doesNotContain("Long");
    }

    @Test
    void deve_responder_500_sem_vazar_a_causa_quando_a_falha_e_inesperada() {
        // Agir
        var resposta = filtro.traduzirFalhaInesperada(
                new IllegalStateException("senha=segredo jdbc:h2:mem:x"), requisicao);

        // Verificar
        assertThat(resposta.getStatusCodeValue()).isEqualTo(500);
        assertThat(corpoDe(resposta).type()).isEqualTo("urn:consumer-pat:erro:CPT-INT-SIS-001");
        assertThat(corpoDe(resposta).toString()).doesNotContain("segredo").doesNotContain("jdbc")
                .doesNotContain("IllegalStateException");
    }
}
