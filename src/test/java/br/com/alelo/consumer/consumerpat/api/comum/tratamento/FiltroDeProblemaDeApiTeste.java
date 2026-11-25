package br.com.alelo.consumer.consumerpat.api.comum.tratamento;

import br.com.alelo.consumer.consumerpat.api.comum.problema.MensagemProblema;
import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercita as traduções que nenhum controller provoca sozinho: falhas de persistência e a falha
 * inesperada. As de entrada e de rota são cobertas pelos testes dos controllers.
 */
class FiltroDeProblemaDeApiTeste {

    private static final String ROTA = "/api/v1/compras";
    private static final MediaType PROBLEMA = MediaType.valueOf("application/problem+json");

    private final FiltroDeProblemaDeApi filtro = new FiltroDeProblemaDeApi();
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
