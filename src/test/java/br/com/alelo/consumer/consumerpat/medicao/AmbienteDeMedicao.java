package br.com.alelo.consumer.consumerpat.medicao;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Descreve a máquina e o ambiente em que a medição rodou, para o relatório dizer onde os números
 * foram obtidos.
 *
 * <p>
 * Tempo absoluto só significa alguma coisa junto da máquina que o produziu: o mesmo código dá
 * outros números num processador mais lento, com menos memória, ou com a frequência da CPU presa no
 * mínimo. O relatório traz o que o Java e o Linux informam sem privilégio: o sistema, o modelo do
 * processador, o governador de frequência, a memória física e a memória dada à JVM. O que só o
 * administrador lê, como o tipo e a velocidade da memória, fica na documentação, descrito à mão.
 *
 * <p>
 * Fora do Linux, o que vem dos arquivos do sistema aparece como {@value #NAO_INFORMADO}, e a
 * medição segue.
 */
public final class AmbienteDeMedicao {

    /** O que aparece quando o ambiente não informa um dado. */
    public static final String NAO_INFORMADO = "não informado";

    private static final Path INFORMACOES_DA_CPU = Path.of("/proc/cpuinfo");

    private static final Path GOVERNADOR_DA_CPU = Path
            .of("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor");

    private static final Locale PORTUGUES = Locale.forLanguageTag("pt-BR");

    private static final long BYTES_POR_MB = 1024L * 1024L;

    private AmbienteDeMedicao() {
    }

    /**
     * As linhas que descrevem o ambiente, prontas para o cabeçalho do relatório.
     *
     * @return o sistema, o processador, a frequência, as memórias e o JDK
     */
    public static List<String> descricao() {
        return List.of("Sistema operacional : " + System.getProperty("os.name") + " "
                + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")",
                "Processador         : " + modeloDoProcessador(INFORMACOES_DA_CPU) + ", "
                        + Runtime.getRuntime().availableProcessors() + " CPUs lógicas",
                "Governador da CPU   : " + primeiraLinha(GOVERNADOR_DA_CPU).orElse(NAO_INFORMADO),
                "Memória física      : " + memoriaFisicaEmMb()
                        .map(mb -> String.format(PORTUGUES, "%,d MB", mb)).orElse(NAO_INFORMADO),
                "JDK                 : " + System.getProperty("java.vm.name") + " "
                        + System.getProperty("java.runtime.version"),
                "Heap do driver      : " + String.format(PORTUGUES, "%,d MB",
                        Runtime.getRuntime().maxMemory() / BYTES_POR_MB),
                "Heap dos sistemas   : " + Optional
                        .ofNullable(System.getenv("MEMORIA_DOS_SISTEMAS")).orElse(NAO_INFORMADO));
    }

    /**
     * O modelo do processador, lido do arquivo de informações da CPU do Linux.
     *
     * @param arquivo o arquivo, que no Linux é {@code /proc/cpuinfo}
     * @return o modelo, ou {@value #NAO_INFORMADO} se o arquivo não existe ou não o traz
     */
    static String modeloDoProcessador(Path arquivo) {
        return valorDaChave(arquivo, "model name").orElse(NAO_INFORMADO);
    }

    /**
     * O valor da primeira linha do tipo {@code chave : valor}.
     *
     * @param arquivo o arquivo
     * @param chave   o que a linha tem antes dos dois-pontos
     * @return o valor sem espaços nas pontas, ou vazio se a linha não existe
     */
    static Optional<String> valorDaChave(Path arquivo, String chave) {
        return linhas(arquivo).stream().filter(linha -> linha.startsWith(chave))
                .map(linha -> linha.substring(linha.indexOf(':') + 1).trim()).findFirst();
    }

    /**
     * A primeira linha de um arquivo, sem espaços nas pontas.
     *
     * @param arquivo o arquivo
     * @return a linha, ou vazio se o arquivo não existe ou está vazio
     */
    static Optional<String> primeiraLinha(Path arquivo) {
        return linhas(arquivo).stream().findFirst().map(String::trim);
    }

    private static List<String> linhas(Path arquivo) {
        try {
            return Files.readAllLines(arquivo);
        } catch (IOException e) {
            return List.of();
        }
    }

    private static Optional<Long> memoriaFisicaEmMb() {
        return Optional.of(ManagementFactory.getOperatingSystemMXBean())
                .filter(com.sun.management.OperatingSystemMXBean.class::isInstance)
                .map(com.sun.management.OperatingSystemMXBean.class::cast)
                .map(sistema -> sistema.getTotalMemorySize() / BYTES_POR_MB);
    }
}
