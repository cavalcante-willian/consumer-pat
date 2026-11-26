package br.com.alelo.consumer.consumerpat.medicao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AmbienteDeMedicaoTeste {

    @TempDir
    Path pasta;

    @Test
    void deve_ler_o_modelo_do_processador_quando_o_arquivo_traz_a_linha() throws IOException {
        // Preparar: o formato de /proc/cpuinfo, com tabulação antes dos dois-pontos
        var cpuinfo = Files.writeString(pasta.resolve("cpuinfo"),
                "processor\t: 0\nmodel name\t: Intel(R) Core(TM) 5 210H\ncpu MHz\t\t: 1200.0\n"
                        + "processor\t: 1\nmodel name\t: Outro Modelo\n");

        // Agir e Verificar: vale a primeira linha, e o valor vem sem espaços nas pontas
        assertThat(AmbienteDeMedicao.modeloDoProcessador(cpuinfo))
                .isEqualTo("Intel(R) Core(TM) 5 210H");
    }

    @Test
    void deve_dizer_nao_informado_quando_o_arquivo_nao_existe() {
        // Agir e Verificar: fora do Linux o arquivo não existe, e a medição segue
        assertThat(AmbienteDeMedicao.modeloDoProcessador(pasta.resolve("nao-existe")))
                .isEqualTo(AmbienteDeMedicao.NAO_INFORMADO);
    }

    @Test
    void deve_dizer_nao_informado_quando_o_arquivo_nao_tem_a_linha() throws IOException {
        // Preparar
        var cpuinfo = Files.writeString(pasta.resolve("cpuinfo"), "processor\t: 0\n");

        // Agir e Verificar
        assertThat(AmbienteDeMedicao.modeloDoProcessador(cpuinfo))
                .isEqualTo(AmbienteDeMedicao.NAO_INFORMADO);
    }

    @Test
    void deve_tirar_o_fim_de_linha_quando_ler_a_primeira_linha() throws IOException {
        // Preparar: o governador de frequência vem numa linha só
        var governador = Files.writeString(pasta.resolve("governador"), "powersave\n");

        // Agir e Verificar
        assertThat(AmbienteDeMedicao.primeiraLinha(governador)).contains("powersave");
    }

    @Test
    void deve_dar_vazio_quando_o_arquivo_esta_vazio_ou_nao_existe() throws IOException {
        // Preparar
        var vazio = Files.writeString(pasta.resolve("vazio"), "");

        // Agir e Verificar
        assertThat(AmbienteDeMedicao.primeiraLinha(vazio)).isEmpty();
        assertThat(AmbienteDeMedicao.primeiraLinha(pasta.resolve("nao-existe"))).isEmpty();
    }

    @Test
    void deve_descrever_o_sistema_o_processador_as_memorias_e_o_jdk_quando_descrever() {
        // Agir
        var linhas = AmbienteDeMedicao.descricao();

        // Verificar: cada dado tem o seu rótulo, e nenhum valor ficou nulo
        assertThat(linhas).hasSize(7);
        assertThat(linhas).extracting(linha -> linha.substring(0, linha.indexOf(':')).trim())
                .containsExactly("Sistema operacional", "Processador", "Governador da CPU",
                        "Memória física", "JDK", "Heap do driver", "Heap dos sistemas");
        assertThat(linhas).noneMatch(linha -> linha.contains("null"));
    }

    @Test
    void deve_dizer_quantas_cpus_e_quanta_memoria_o_driver_tem_quando_descrever() {
        // Agir
        var linhas = AmbienteDeMedicao.descricao();

        // Verificar
        assertThat(linhas.get(1))
                .contains(Runtime.getRuntime().availableProcessors() + " CPUs lógicas");
        assertThat(linhas.get(5)).endsWith(" MB");
    }
}
