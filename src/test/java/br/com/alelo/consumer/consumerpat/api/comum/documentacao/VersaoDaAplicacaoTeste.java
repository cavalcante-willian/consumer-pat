package br.com.alelo.consumer.consumerpat.api.comum.documentacao;

import br.com.alelo.consumer.consumerpat.InicializacaoDaAplicacao;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.assertj.core.api.Assertions.assertThat;

class VersaoDaAplicacaoTeste {

    @Test
    void deve_ler_a_versao_do_manifesto_quando_a_classe_vem_de_um_jar_com_versao() {
        // Agir: o jar do Spring Boot declara o Implementation-Version
        var versao = VersaoDaAplicacao.de(SpringApplication.class);

        // Verificar
        assertThat(versao).matches("\\d+\\.\\d+\\.\\d+.*");
    }

    @Test
    void deve_dizer_que_nao_ha_versao_quando_a_classe_vem_do_diretorio_de_classes() {
        // Agir: nos testes a aplicação roda das classes compiladas, sem manifesto
        var versao = VersaoDaAplicacao.de(InicializacaoDaAplicacao.class);

        // Verificar
        assertThat(versao).isEqualTo(VersaoDaAplicacao.SEM_VERSAO);
    }

    @Test
    void deve_dizer_que_nao_ha_versao_quando_a_classe_nao_tem_pacote() {
        // Agir: um array não pertence a nenhum pacote
        var versao = VersaoDaAplicacao.de(int[].class);

        // Verificar
        assertThat(versao).isEqualTo(VersaoDaAplicacao.SEM_VERSAO);
    }
}
