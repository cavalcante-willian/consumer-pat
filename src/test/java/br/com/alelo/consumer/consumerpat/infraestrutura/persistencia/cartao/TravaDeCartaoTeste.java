package br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.cartao;

import br.com.alelo.consumer.consumerpat.api.configuracao.ConfiguracaoDeAuditoria;
import br.com.alelo.consumer.consumerpat.dominio.cartao.NumeroDeCartao;
import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.fabricas.FabricaDeCartoes;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.ConfiguracaoDeRelogioDeTeste;
// editorconfig-checker-disable-next-line
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ConsumidorRepositorioJpa;
import br.com.alelo.consumer.consumerpat.infraestrutura.persistencia.consumidor.ConsumidoresDeTeste;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Prova que a trava do cartão existe e que a espera por ela tem teto. O teto é de meio segundo, e
 * não o de produção, para que o teste seja rápido e para distinguir o teto configurado do padrão do
 * banco: se a dica fosse ignorada em silêncio, a espera seguiria o padrão do banco, mais longo, e o
 * teste falharia por demorar. Um timeout que não dispara não é um timeout.
 *
 * <p>
 * Roda fora da transação do teste, porque a trava vive numa transação própria de cada lado, e limpa
 * o que gravou, já que gravou de verdade.
 */
@DataJpaTest(properties = TravaDeCartaoTeste.URL_COM_TETO_CURTO)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({ ConfiguracaoDeAuditoria.class, ConfiguracaoDeRelogioDeTeste.class })
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class TravaDeCartaoTeste {

    /** O banco do teste, com o teto de meio segundo; o resto da configuração é a de produção. */
    static final String URL_COM_TETO_CURTO = "spring.datasource.url="
            + "jdbc:h2:mem:trava-de-cartao;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=500";

    private static final NumeroDeCartao NUMERO = new NumeroDeCartao(FabricaDeCartoes.NUMERO_PADRAO);

    @Autowired
    private CartaoRepositorioJpa repositorio;

    @Autowired
    private ConsumidorRepositorioJpa consumidores;

    @Autowired
    private PlatformTransactionManager transacoes;

    @BeforeEach
    void gravarOCartao() {
        var dono = consumidores.saveAndFlush(ConsumidoresDeTeste.linha());
        repositorio.saveAndFlush(CartoesDeTeste.linha(dono, TipoCartao.ALIMENTACAO, "100.00"));
    }

    @AfterEach
    void apagarOCartao() {
        repositorio.deleteAll();
        consumidores.deleteAll();
    }

    @Test
    void deve_falhar_dentro_do_teto_quando_o_cartao_esta_travado_por_outra_transacao()
            throws Exception {
        // Preparar
        var adaptador = new CartaoAdaptador(repositorio);
        var transacao = new TransactionTemplate(transacoes);
        var travado = new CountDownLatch(1);
        var liberar = new CountDownLatch(1);
        var executor = Executors.newSingleThreadExecutor();
        var dona = executor.submit(() -> transacao.executeWithoutResult(estado -> {
            adaptador.buscarPorNumeroETipoParaAtualizacao(NUMERO, TipoCartao.ALIMENTACAO);
            travado.countDown();
            aguardar(liberar);
        }));
        assertThat(travado.await(10, TimeUnit.SECONDS)).isTrue();

        // Agir
        var inicio = System.nanoTime();
        var falha = catchThrowable(() -> transacao.executeWithoutResult(estado -> adaptador
                .buscarPorNumeroETipoParaAtualizacao(NUMERO, TipoCartao.ALIMENTACAO)));
        var espera = Duration.ofNanos(System.nanoTime() - inicio);
        liberar.countDown();
        dona.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Verificar
        assertThat(falha).isInstanceOf(PessimisticLockingFailureException.class);
        assertThat(espera).isLessThan(Duration.ofMillis(1500));
    }

    @Test
    void deve_esperar_e_ter_sucesso_quando_a_outra_transacao_termina_antes_do_teto()
            throws Exception {
        // Preparar
        var adaptador = new CartaoAdaptador(repositorio);
        var transacao = new TransactionTemplate(transacoes);
        var travado = new CountDownLatch(1);
        var executor = Executors.newSingleThreadExecutor();
        var dona = executor.submit(() -> transacao.executeWithoutResult(estado -> {
            adaptador.buscarPorNumeroETipoParaAtualizacao(NUMERO, TipoCartao.ALIMENTACAO);
            travado.countDown();
            dormir(Duration.ofMillis(150));
        }));
        assertThat(travado.await(10, TimeUnit.SECONDS)).isTrue();

        // Agir
        var falha = catchThrowable(() -> transacao.executeWithoutResult(estado -> adaptador
                .buscarPorNumeroETipoParaAtualizacao(NUMERO, TipoCartao.ALIMENTACAO)));
        dona.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Verificar
        assertThat(falha).isNull();
    }

    private static void aguardar(CountDownLatch trava) {
        try {
            trava.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void dormir(Duration duracao) {
        try {
            Thread.sleep(duracao.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
