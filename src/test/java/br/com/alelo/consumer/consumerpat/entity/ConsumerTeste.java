package br.com.alelo.consumer.consumerpat.entity;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Caracteriza a igualdade do {@link Consumer}. É teste unitário puro: nenhum contexto Spring.
 */
class ConsumerTeste {

    private static Consumer consumidor(String nome, int documento) {
        var consumidor = new Consumer();
        consumidor.setName(nome);
        consumidor.setDocumentNumber(documento);
        return consumidor;
    }

    @Test
    void deve_considerar_iguais_dois_consumidores_quando_os_dados_sao_os_mesmos() {
        assertThat(consumidor("Ana", 1)).isEqualTo(consumidor("Ana", 1));
    }

    @Test
    void deve_considerar_diferentes_dois_consumidores_quando_o_nome_difere() {
        assertThat(consumidor("Ana", 1)).isNotEqualTo(consumidor("Bia", 1));
    }

    @Test
    void deve_considerar_diferentes_dois_consumidores_quando_o_saldo_difere() {
        var comSaldo = consumidor("Ana", 1);
        comSaldo.setFoodCardBalance(10.0);

        assertThat(comSaldo).isNotEqualTo(consumidor("Ana", 1));
    }

    @Test
    @Disabled("Defeito do legado: equals foi sobrescrito sem hashCode (o Lombok avisa e não "
            + "gera o hashCode), então dois consumidores iguais têm hashes diferentes e "
            + "quebram HashSet e HashMap. Habilitar no commit que corrige.")
    void deve_ter_o_mesmo_hash_quando_dois_consumidores_sao_iguais() {
        assertThat(consumidor("Ana", 1)).hasSameHashCodeAs(consumidor("Ana", 1));
    }
}
