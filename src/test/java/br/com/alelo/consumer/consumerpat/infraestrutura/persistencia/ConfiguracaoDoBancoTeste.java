package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guarda a configuração do banco contra a deriva: os tetos de espera existem, o de trava está onde
 * o H2 o respeita, e a espera por conexão é maior que a espera por trava.
 *
 * <p>
 * Lê o {@code application.yml} de produção, e não uma cópia, porque é ele que vale. O comportamento
 * do teto, que ele dispara de verdade, é provado em {@code TravaDeCartaoTeste}.
 */
class ConfiguracaoDoBancoTeste {

    private static final Pattern TETO_DE_TRAVA = Pattern.compile("LOCK_TIMEOUT=(\\d+)");

    private static Properties configuracao() {
        var leitor = new YamlPropertiesFactoryBean();
        leitor.setResources(new ClassPathResource("config/application.yml"));
        return leitor.getObject();
    }

    private static int tetoDeTravaEmMilissegundos() {
        var url = configuracao().getProperty("spring.datasource.url");
        var encontrado = TETO_DE_TRAVA.matcher(url);
        assertThat(encontrado.find()).as("a URL do banco declara LOCK_TIMEOUT: %s", url).isTrue();
        return Integer.parseInt(encontrado.group(1));
    }

    @Test
    void deve_declarar_o_teto_da_trava_na_url_quando_ler_a_configuracao() {
        // Verificar
        assertThat(tetoDeTravaEmMilissegundos()).isPositive();
    }

    @Test
    void deve_esperar_mais_pela_conexao_do_que_pela_trava_quando_comparar_os_tetos() {
        // Preparar
        var esperaPorConexao = Integer.parseInt(
                configuracao().getProperty("spring.datasource.hikari.connection-timeout"));

        // Verificar
        assertThat(esperaPorConexao).isGreaterThan(tetoDeTravaEmMilissegundos());
    }

    @Test
    void deve_ligar_o_avaliador_de_erro_de_trava_ao_pool_quando_ler_a_configuracao() {
        // Agir
        var classe = configuracao()
                .getProperty("spring.datasource.hikari.exception-override-class-name");

        // Verificar
        assertThat(classe).isEqualTo(ErroDeTravaNaoDescartaConexao.class.getName());
    }

    @Test
    void deve_gravar_instantes_em_utc_quando_ler_a_configuracao() {
        // Verificar
        assertThat(configuracao().getProperty("spring.jpa.properties.hibernate.jdbc.time_zone"))
                .isEqualTo("UTC");
    }
}
