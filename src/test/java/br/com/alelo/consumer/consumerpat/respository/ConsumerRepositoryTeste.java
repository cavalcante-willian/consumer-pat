package br.com.alelo.consumer.consumerpat.respository;

import br.com.alelo.consumer.consumerpat.entity.Consumer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Caracteriza o acesso a dados do legado contra um banco real (H2 em memória), sem mock de
 * repositório: o que se afirma é o que o banco de fato devolve e aceita.
 */
@DataJpaTest
class ConsumerRepositoryTeste {

    private static final AtomicInteger PROXIMO_NUMERO = new AtomicInteger(5000);

    @Autowired
    ConsumerRepository consumidores;

    private Consumer consumidorSalvo(int documento, int cartaoAlimentacao, int cartaoCombustivel,
            int cartaoFarmacia) {
        var consumidor = new Consumer();
        consumidor.setName("Consumidor " + documento);
        consumidor.setDocumentNumber(documento);
        consumidor.setFoodCardNumber(cartaoAlimentacao);
        consumidor.setFuelCardNumber(cartaoCombustivel);
        consumidor.setDrugstoreNumber(cartaoFarmacia);
        return consumidores.saveAndFlush(consumidor);
    }

    private Consumer consumidorComCartoesExclusivos() {
        return consumidorSalvo(PROXIMO_NUMERO.incrementAndGet(), PROXIMO_NUMERO.incrementAndGet(),
                PROXIMO_NUMERO.incrementAndGet(), PROXIMO_NUMERO.incrementAndGet());
    }

    @Test
    void deve_achar_o_consumidor_quando_buscar_pelo_cartao_de_alimentacao() {
        var consumidor = consumidorComCartoesExclusivos();

        var achado = consumidores.findByFoodCardNumber(consumidor.getFoodCardNumber());

        assertThat(achado.getId()).isEqualTo(consumidor.getId());
    }

    @Test
    void deve_achar_o_consumidor_quando_buscar_pelo_cartao_de_combustivel() {
        var consumidor = consumidorComCartoesExclusivos();

        var achado = consumidores.findByFuelCardNumber(consumidor.getFuelCardNumber());

        assertThat(achado.getId()).isEqualTo(consumidor.getId());
    }

    @Test
    void deve_achar_o_consumidor_quando_buscar_pelo_cartao_de_farmacia() {
        var consumidor = consumidorComCartoesExclusivos();

        var achado = consumidores.findByDrugstoreNumber(consumidor.getDrugstoreNumber());

        assertThat(achado.getId()).isEqualTo(consumidor.getId());
    }

    @Test
    void deve_devolver_nulo_quando_o_cartao_nao_existe() {
        assertThat(consumidores.findByFoodCardNumber(987654321)).isNull();
    }

    @Test
    void deve_listar_o_consumidor_quando_buscar_todos() {
        var consumidor = consumidorComCartoesExclusivos();

        assertThat(consumidores.getAllConsumersList()).extracting(Consumer::getId)
                .contains(consumidor.getId());
    }

    @Test
    @Disabled("Defeito do legado: o número do cartão não tem restrição de unicidade, então dois "
            + "consumidores podem ter o mesmo cartão e a busca nativa passa a falhar com mais "
            + "de uma linha. Habilitar no commit que cria a restrição.")
    void deve_recusar_o_segundo_consumidor_quando_o_cartao_de_alimentacao_ja_existe() {
        var primeiro = consumidorComCartoesExclusivos();

        assertThatThrownBy(() -> consumidorSalvo(PROXIMO_NUMERO.incrementAndGet(),
                primeiro.getFoodCardNumber(), PROXIMO_NUMERO.incrementAndGet(),
                PROXIMO_NUMERO.incrementAndGet()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Disabled("Defeito do legado: o documento não tem restrição de unicidade, então o mesmo "
            + "documento cadastra dois consumidores. Habilitar no commit que cria a restrição.")
    void deve_recusar_o_segundo_consumidor_quando_o_documento_ja_existe() {
        var primeiro = consumidorComCartoesExclusivos();

        assertThatThrownBy(() -> consumidorSalvo(primeiro.getDocumentNumber(),
                PROXIMO_NUMERO.incrementAndGet(), PROXIMO_NUMERO.incrementAndGet(),
                PROXIMO_NUMERO.incrementAndGet()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
