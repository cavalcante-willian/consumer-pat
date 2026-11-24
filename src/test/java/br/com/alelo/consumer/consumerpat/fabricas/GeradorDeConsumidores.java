package br.com.alelo.consumer.consumerpat.fabricas;

import br.com.alelo.consumer.consumerpat.entity.Consumer;
import br.com.alelo.consumer.consumerpat.respository.ConsumerRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Gera consumidores em volume para medir desempenho. Vive só em {@code src/test}: o dado é
 * construído em código, nunca carregado de arquivo, e não faz parte da aplicação.
 *
 * <p>
 * Os números de documento e de cartão são sequenciais a partir de uma base, então são únicos e
 * previsíveis: o consumidor de índice {@code i} tem os cartões {@code base + 3i},
 * {@code base + 3i + 1} e {@code base + 3i + 2}, o que permite escolher um registro do meio ou do
 * fim da tabela sem consultar o banco.
 */
public final class GeradorDeConsumidores {

    private static final int TAMANHO_DO_LOTE = 1_000;

    private GeradorDeConsumidores() {
    }

    /** Número do cartão de alimentação do consumidor de índice {@code indice}. */
    public static int cartaoDeAlimentacao(int base, int indice) {
        return base + 3 * indice;
    }

    /** Número do cartão de combustível do consumidor de índice {@code indice}. */
    public static int cartaoDeCombustivel(int base, int indice) {
        return base + 3 * indice + 1;
    }

    /** Número do cartão de farmácia do consumidor de índice {@code indice}. */
    public static int cartaoDeFarmacia(int base, int indice) {
        return base + 3 * indice + 2;
    }

    /**
     * Grava {@code quantidade} consumidores, em lotes, e devolve quantos foram gravados.
     */
    public static int gravar(ConsumerRepository repositorio, int quantidade, int base) {
        for (int inicio = 0; inicio < quantidade; inicio += TAMANHO_DO_LOTE) {
            var fim = Math.min(inicio + TAMANHO_DO_LOTE, quantidade);
            List<Consumer> lote = new ArrayList<>(fim - inicio);
            for (int i = inicio; i < fim; i++) {
                lote.add(consumidor(base, i));
            }
            repositorio.saveAll(lote);
        }
        return quantidade;
    }

    private static Consumer consumidor(int base, int indice) {
        var consumidor = new Consumer();
        consumidor.setName("Consumidor de medição " + indice);
        consumidor.setDocumentNumber(base + indice);
        consumidor.setBirthDate(new Date(0L));
        consumidor.setMobilePhoneNumber(base + indice);
        consumidor.setResidencePhoneNumber(base + indice);
        consumidor.setPhoneNumber(base + indice);
        consumidor.setEmail("consumidor" + indice + "@exemplo.com");
        consumidor.setStreet("Rua " + indice);
        consumidor.setNumber(indice);
        consumidor.setCity("Cidade");
        consumidor.setCountry("Brasil");
        consumidor.setPortalCode(indice);
        consumidor.setFoodCardNumber(cartaoDeAlimentacao(base, indice));
        consumidor.setFoodCardBalance(1000.0);
        consumidor.setFuelCardNumber(cartaoDeCombustivel(base, indice));
        consumidor.setFuelCardBalance(1000.0);
        consumidor.setDrugstoreNumber(cartaoDeFarmacia(base, indice));
        consumidor.setDrugstoreCardBalance(1000.0);
        return consumidor;
    }
}
