package br.com.alelo.consumer.consumerpat.infraestrutura.log;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova o perfil {@code prod}: nenhum pacote do projeto abaixo de INFO, e a rotação do arquivo por
 * propriedade. O destino do arquivo é trocado por um caminho em {@code build}, para o teste não
 * criar {@code logs/} na raiz do projeto.
 */
@SpringBootTest(properties = "logging.file.name=build/tmp/consumer-pat-prod-teste.log")
@ActiveProfiles("prod")
class ConfiguracaoDeLogDeProducaoTeste {

    @Autowired
    private Environment ambiente;

    @Test
    void deve_manter_prod_em_info_quando_perfil_prod() {
        // Agir
        var logger = LoggerFactory.getLogger("br.com.alelo.consumer.consumerpat.qualquer.Classe");

        // Verificar: nenhum pacote do projeto abaixo de INFO, e a depuração não existe
        assertThat(logger.isDebugEnabled()).isFalse();
        assertThat(logger.isInfoEnabled()).isTrue();
        assertThat(ambiente.getProperty("logging.level.br.com.alelo.consumer")).isEqualTo("INFO");
    }

    @Test
    void deve_manter_o_log_do_evento_em_info_quando_perfil_prod() {
        // Agir
        var logger = LoggerFactory.getLogger(SaidaDeLogAdaptador.NOME_DO_LOGGER);

        // Verificar
        assertThat(logger.isDebugEnabled()).isFalse();
        assertThat(logger.isInfoEnabled()).isTrue();
    }

    @Test
    void deve_rotacionar_o_arquivo_por_tamanho_e_por_historico_quando_perfil_prod() {
        // Verificar: arquivo sem rotação cresce até encher o disco
        assertThat(ambiente.getProperty("logging.logback.rollingpolicy.max-file-size"))
                .isEqualTo("10MB");
        assertThat(ambiente.getProperty("logging.logback.rollingpolicy.max-history"))
                .isEqualTo("14");
        assertThat(ambiente.getProperty("logging.logback.rollingpolicy.total-size-cap"))
                .isEqualTo("500MB");
    }

    @Test
    void deve_gravar_o_arquivo_em_logs_quando_ler_o_perfil_prod_sem_a_troca_do_teste() {
        // Preparar: lê o arquivo do perfil, e não o ambiente, onde o teste trocou o destino
        var fabrica = new YamlPropertiesFactoryBean();
        fabrica.setResources(new ClassPathResource("config/application-prod.yml"));

        // Verificar
        assertThat(fabrica.getObject().getProperty("logging.file.name"))
                .isEqualTo("logs/consumer-pat.log");
    }
}
