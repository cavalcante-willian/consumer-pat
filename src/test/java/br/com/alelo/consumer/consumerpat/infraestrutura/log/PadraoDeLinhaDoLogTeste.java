package br.com.alelo.consumer.consumerpat.infraestrutura.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova os padrões de linha do {@code application.yml} montando o layout do Logback direto deles,
 * sem contexto Spring. O Spring Boot só aplica padrão e arquivo na primeira inicialização do
 * Logback da JVM, e um teste por contexto dependeria da ordem em que as classes de teste rodam.
 */
class PadraoDeLinhaDoLogTeste {

    private final LoggerContext contexto = new LoggerContext();

    private String padrao(String propriedade) {
        var fabrica = new YamlPropertiesFactoryBean();
        fabrica.setResources(new ClassPathResource("config/application.yml"));
        return fabrica.getObject().getProperty(propriedade);
    }

    private String formatar(String propriedade, String mensagem, Map<String, String> mdc) {
        var layout = new PatternLayout();
        layout.setContext(contexto);
        layout.setPattern(padrao(propriedade));
        layout.start();
        var evento = new LoggingEvent(PadraoDeLinhaDoLogTeste.class.getName(),
                contexto.getLogger("br.com.alelo.consumer.consumerpat.eventos"), Level.INFO,
                mensagem, null, null);
        evento.setMDCPropertyMap(mdc);
        return layout.doLayout(evento);
    }

    @BeforeEach
    void iniciar() {
        contexto.start();
    }

    @ParameterizedTest
    @ValueSource(strings = { "logging.pattern.console", "logging.pattern.file" })
    void deve_mascarar_a_sequencia_de_cartao_quando_o_padrao_formata_a_linha(String propriedade) {
        // Agir
        var linha = formatar(propriedade, "x observacao=cartao 4111111111111111 fim", Map.of());

        // Verificar
        assertThat(linha).contains("observacao=cartao *** fim").doesNotContain("4111111111111111");
    }

    @ParameterizedTest
    @ValueSource(strings = { "logging.pattern.console", "logging.pattern.file" })
    void deve_mascarar_a_sequencia_de_documento_quando_o_padrao_formata_a_linha(
            String propriedade) {
        // Agir
        var linha = formatar(propriedade, "cpf 12345678901 e cnpj 11222333000181", Map.of());

        // Verificar
        assertThat(linha).contains("cpf *** e cnpj ***").doesNotContain("12345678901");
    }

    @ParameterizedTest
    @ValueSource(
            strings = { "1234567890", "123456789012", "12345678901234567890", "abc12345678901" })
    void deve_manter_a_sequencia_quando_ela_nao_tem_o_tamanho_de_documento_nem_de_cartao(
            String sequencia) {
        // Agir
        var linha = formatar("logging.pattern.console", "valor " + sequencia, Map.of());

        // Verificar: 10, 12 e 20 dígitos, e dígitos colados em letras, não são mascarados
        assertThat(linha).contains(sequencia);
    }

    @Test
    void deve_levar_o_id_da_requisicao_quando_o_mdc_tem_o_id() {
        // Agir
        var linha = formatar("logging.pattern.console", "x", Map.of("idRequisicao", "abc-123"));

        // Verificar
        assertThat(linha).contains(" INFO [abc-123] ");
    }

    @Test
    void deve_levar_o_marcador_de_sem_requisicao_quando_o_mdc_esta_vazio() {
        // Agir
        var linha = formatar("logging.pattern.file", "x", Map.of());

        // Verificar
        assertThat(linha).contains(" INFO [sem-requisicao] ");
    }

    @Test
    void deve_escrever_a_data_em_utc_quando_o_padrao_formata_a_linha() {
        // Agir
        var linha = formatar("logging.pattern.console", "x", Map.of());

        // Verificar
        assertThat(linha).matches("(?s)\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z .*");
    }

    @Test
    void deve_usar_o_mesmo_padrao_no_console_e_no_arquivo() {
        // Verificar: a barreira de máscara não pode faltar em um dos dois destinos
        assertThat(padrao("logging.pattern.file")).isEqualTo(padrao("logging.pattern.console"));
    }
}
