package br.com.alelo.consumer.consumerpat.controller;

import br.com.alelo.consumer.consumerpat.fabricas.GeradorDeConsumidores;
import br.com.alelo.consumer.consumerpat.respository.ConsumerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Mede o desempenho do legado sobre 50.000 consumidores, para registrar a linha
 * de base antes de
 * qualquer refatoração. Não é teste de regra: nenhum valor de tempo é afirmado,
 * porque o tempo
 * depende da máquina. O resultado vai para
 * {@code build/medicao/linha-de-base-do-legado.txt}.
 *
 * <p>
 * Fica desligado por padrão, por ser lento. Para rodar:
 * {@code MEDICAO=1 ./gradlew test --tests '*LinhaDeBaseDoLegadoTeste'}. A
 * duração é medida com
 * {@link System#nanoTime()}, que é monotônico, e a mediana é tomada depois de
 * descartar o
 * aquecimento, que paga carregamento de classe e abertura de conexão.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "MEDICAO", matches = "1")
class LinhaDeBaseDoLegadoTeste {

    private static final int CONSUMIDORES = 50_000;
    private static final int BASE = 10_000_000;
    private static final int AQUECIMENTO = 3;
    private static final int MEDICOES = 10;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ConsumerRepository consumidores;

    @Autowired
    JdbcTemplate jdbc;

    private static double emMilissegundos(long nanos) {
        return Duration.ofNanos(nanos).toNanos() / 1_000_000.0;
    }

    /**
     * Executa a ação {@code AQUECIMENTO} vezes sem medir e {@code MEDICOES} vezes
     * medindo.
     */
    private static List<Double> medir(Callable<?> acao) throws Exception {
        for (int i = 0; i < AQUECIMENTO; i++) {
            acao.call();
        }
        List<Double> tempos = new ArrayList<>();
        for (int i = 0; i < MEDICOES; i++) {
            var inicio = System.nanoTime();
            acao.call();
            tempos.add(emMilissegundos(System.nanoTime() - inicio));
        }
        Collections.sort(tempos);
        return tempos;
    }

    private static String resumo(String rotulo, List<Double> tempos) {
        var mediana = (tempos.get(MEDICOES / 2 - 1) + tempos.get(MEDICOES / 2)) / 2.0;
        return String.format("%-44s mediana %9.2f ms | mín %9.2f ms | máx %9.2f ms", rotulo, mediana,
                tempos.get(0), tempos.get(MEDICOES - 1));
    }

    @Test
    void deve_registrar_a_linha_de_base_do_legado_com_cinquenta_mil_consumidores() throws Exception {
        var relatorio = new ArrayList<String>();
        relatorio.add("Linha de base do legado — " + CONSUMIDORES + " consumidores");
        relatorio.add("JDK " + System.getProperty("java.version") + " | " + Runtime.getRuntime().availableProcessors()
                + " CPUs | heap máximo " + Runtime.getRuntime().maxMemory() / (1024 * 1024)
                + " MB | H2 em memória, MockMvc");
        relatorio.add("Aquecimento: " + AQUECIMENTO + " chamadas descartadas; medições: " + MEDICOES + " por caminho");
        relatorio.add("");

        var inicioDaGeracao = System.nanoTime();
        GeradorDeConsumidores.gravar(consumidores, CONSUMIDORES, BASE);
        relatorio.add(String.format("Geração dos %d consumidores: %.0f ms", CONSUMIDORES,
                emMilissegundos(System.nanoTime() - inicioDaGeracao)));
        assertThat(consumidores.count()).isGreaterThanOrEqualTo(CONSUMIDORES);

        var tamanhos = new ArrayList<Integer>();
        var listagem = medir(() -> {
            var corpo = mockMvc.perform(get("/consumer/consumerList")).andReturn().getResponse()
                    .getContentAsByteArray();
            tamanhos.add(corpo.length);
            return corpo;
        });
        relatorio.add(resumo("GET /consumer/consumerList", listagem));
        relatorio.add("   resposta: " + tamanhos.get(0) / (1024 * 1024) + " MB (" + tamanhos.get(0)
                + " bytes) em uma só resposta");

        var meio = CONSUMIDORES / 2;
        var fim = CONSUMIDORES - 1;
        relatorio.add(resumo("busca por cartão de alimentação (meio)",
                medir(() -> consumidores.findByFoodCardNumber(GeradorDeConsumidores.cartaoDeAlimentacao(BASE, meio)))));
        relatorio.add(resumo("busca por cartão de alimentação (fim)",
                medir(() -> consumidores.findByFoodCardNumber(GeradorDeConsumidores.cartaoDeAlimentacao(BASE, fim)))));
        relatorio.add(resumo("busca por cartão de farmácia (fim)",
                medir(() -> consumidores.findByDrugstoreNumber(GeradorDeConsumidores.cartaoDeFarmacia(BASE, fim)))));
        relatorio.add(resumo("GET /consumer/buy (cartão do meio)", medir(() -> mockMvc.perform(get("/consumer/buy")
                .param("establishmentType", "1")
                .param("establishmentName", "Estabelecimento")
                .param("cardNumber", String.valueOf(GeradorDeConsumidores.cartaoDeAlimentacao(BASE, meio)))
                .param("productDescription", "Produto")
                .param("value", "1.0")).andReturn())));

        var plano = jdbc.queryForObject(
                "EXPLAIN SELECT * FROM CONSUMER WHERE FOOD_CARD_NUMBER = ?", String.class, 1);
        relatorio.add("");
        relatorio.add("Plano do H2 para a busca por cartão: " + plano.replaceAll("\\s+", " "));

        var arquivo = Path.of("build", "medicao", "linha-de-base-do-legado.txt");
        Files.createDirectories(arquivo.getParent());
        Files.write(arquivo, relatorio);
    }
}
