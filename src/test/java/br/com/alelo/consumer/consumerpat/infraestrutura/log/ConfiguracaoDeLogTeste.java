package br.com.alelo.consumer.consumerpat.infraestrutura.log;

import br.com.alelo.consumer.consumerpat.dominio.log.SaidaDeLogPorta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.env.Environment;

import static br.com.alelo.consumer.consumerpat.dominio.log.CampoDeLog.campo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova a configuração do log do perfil padrão no contexto inteiro: o padrão de linha com a
 * barreira de máscara, a depuração só aqui e nenhum {@code show-sql}.
 */
@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class ConfiguracaoDeLogTeste {

    @Autowired
    private SaidaDeLogPorta saida;

    @Autowired
    private Environment ambiente;

    @Test
    void deve_mascarar_a_sequencia_de_digitos_de_cartao_quando_ela_vem_em_texto_livre(
            CapturedOutput saidaDoConsole) {
        // Agir: o nome do campo não é sensível, então só a barreira do padrão pode mascarar
        saida.informacao("teste.mascara", campo("observacao", "pago com 5555000011112222 ok"));

        // Verificar
        assertThat(saidaDoConsole.getOut()).contains("observacao=pago com *** ok")
                .doesNotContain("5555000011112222");
    }

    @Test
    void deve_mascarar_a_sequencia_de_onze_e_de_catorze_digitos_quando_ela_vem_em_texto_livre(
            CapturedOutput saidaDoConsole) {
        // Agir
        saida.informacao("teste.documento",
                campo("observacao", "cpf 12345678901 cnpj 11222333000181"));

        // Verificar: 11 dígitos (documento) e 14 dígitos (CNPJ, dentro da faixa 13 a 19)
        assertThat(saidaDoConsole.getOut()).contains("observacao=cpf *** cnpj ***")
                .doesNotContain("12345678901").doesNotContain("11222333000181");
    }

    @Test
    void deve_manter_os_numeros_curtos_quando_o_valor_nao_parece_documento_nem_cartao(
            CapturedOutput saidaDoConsole) {
        // Agir
        saida.informacao("teste.curto", campo("consumidorId", 42), campo("duracaoMs", 1234));

        // Verificar
        assertThat(saidaDoConsole.getOut()).contains("consumidorId=42 duracaoMs=1234");
    }

    @Test
    void deve_levar_o_marcador_de_sem_requisicao_e_a_data_em_utc_quando_nao_ha_requisicao(
            CapturedOutput saidaDoConsole) {
        // Agir
        saida.informacao("teste.formato");

        // Verificar
        assertThat(saidaDoConsole.getOut()).containsPattern(
                "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z\\s+INFO \\[sem-requisicao\\] "
                        + ".* - teste.formato");
    }

    @Test
    void deve_manter_a_depuracao_ligada_so_no_perfil_padrao() {
        // Verificar
        assertThat(ambiente.getProperty("logging.level.br.com.alelo.consumer")).isEqualTo("DEBUG");
        assertThat(ambiente.getProperty("logging.level.root")).isEqualTo("INFO");
    }

    @Test
    void deve_nao_imprimir_o_sql_nem_os_valores_vinculados_quando_a_configuracao_e_lida() {
        // Verificar: show-sql e o TRACE do binder imprimiriam os valores das consultas
        assertThat(ambiente.getProperty("spring.jpa.show-sql", Boolean.class, false)).isFalse();
        assertThat(ambiente.getProperty("logging.level.org.hibernate.type.descriptor.sql"))
                .isNull();
        assertThat(ambiente.getProperty("logging.level.org.hibernate.SQL")).isNull();
    }
}
