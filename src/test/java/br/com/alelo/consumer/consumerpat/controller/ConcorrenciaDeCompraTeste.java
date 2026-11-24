package br.com.alelo.consumer.consumerpat.controller;

import br.com.alelo.consumer.consumerpat.entity.Consumer;
import br.com.alelo.consumer.consumerpat.respository.ConsumerRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Caracteriza o comportamento do saldo sob acesso simultâneo: a invariante é que cada compra
 * confirmada reduz o saldo, sem que uma sobrescreva a outra.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ConcorrenciaDeCompraTeste {

    private static final int COMPRAS = 40;
    private static final int THREADS = 8;
    private static final int CARTAO_DE_FARMACIA = 7_000_001;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ConsumerRepository consumidores;

    @Test
    @Disabled("Defeito do legado: a compra lê o saldo, subtrai em memória e grava, sem transação nem "
        + "lock, então compras simultâneas no mesmo cartão se sobrescrevem (lost update) e o saldo final "
        + "fica acima do correto. Habilitar no commit que introduz o lock pessimista.")
    void deve_debitar_todas_as_compras_quando_chegam_simultaneamente_no_mesmo_cartao() throws Exception {
        var consumidor = new Consumer();
        consumidor.setName("Consumidor concorrente");
        consumidor.setDocumentNumber(7_000_000);
        consumidor.setDrugstoreNumber(CARTAO_DE_FARMACIA);
        consumidor.setDrugstoreCardBalance(1000.0);
        var salvo = consumidores.save(consumidor);

        var pool = Executors.newFixedThreadPool(THREADS);
        var largada = new CountDownLatch(1);
        List<Future<?>> compras = new ArrayList<>();
        for (int i = 0; i < COMPRAS; i++) {
            compras.add(pool.submit(() -> {
                largada.await();
                mockMvc.perform(get("/consumer/buy")
                        .param("establishmentType", "2")
                        .param("establishmentName", "Farmácia")
                        .param("cardNumber", String.valueOf(CARTAO_DE_FARMACIA))
                        .param("productDescription", "Produto")
                        .param("value", "1.0"))
                    .andReturn();
                return null;
            }));
        }
        largada.countDown();
        for (var compra : compras) {
            compra.get(30, TimeUnit.SECONDS);
        }
        pool.shutdown();

        var saldo = consumidores.findById(salvo.getId()).orElseThrow().getDrugstoreCardBalance();
        assertThat(saldo).isEqualTo(1000.0 - COMPRAS);
    }
}
