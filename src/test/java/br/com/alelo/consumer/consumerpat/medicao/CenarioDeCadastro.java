package br.com.alelo.consumer.consumerpat.medicao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Cadastra a base inteira, um consumidor por vez, medindo cada cadastro.
 *
 * <p>
 * É também a semeadura: os cenários seguintes medem sobre a base que este deixa pronta. Não há
 * aquecimento descartado, porque as dezenas de milhares de chamadas já são a amostra, e o percentil
 * 95 delas diz mais que dez medidas. O avanço sai no console a cada bloco, porque a semeadura leva
 * minutos.
 */
public final class CenarioDeCadastro implements Cenario {

    private static final Logger LOG = LoggerFactory.getLogger(CenarioDeCadastro.class);

    private static final int TAMANHO_DO_BLOCO = 5_000;

    private static final Locale PORTUGUES = Locale.forLanguageTag("pt-BR");

    private static final double MILISSEGUNDOS_POR_SEGUNDO = 1_000.0;

    private final int total;

    /**
     * Cria o cenário sobre uma base.
     *
     * @param total quantos consumidores cadastrar
     */
    public CenarioDeCadastro(int total) {
        this.total = total;
    }

    @Override
    public List<Medida> medir(OperacoesDoAlvo alvo) throws Exception {
        var cadastros = new ArrayList<Cronometrado<Integer>>();
        for (int bloco = 0; bloco < total; bloco += TAMANHO_DO_BLOCO) {
            var fim = Math.min(total, bloco + TAMANHO_DO_BLOCO);
            for (int indice = bloco; indice < fim; indice++) {
                var consumidor = indice;
                cadastros
                        .add(MedidorDeTempo.cronometrar(() -> alvo.cadastrar(consumidor).status()));
            }
            LOG.info("[{}] cadastro: {} de {}", alvo.nome(), fim, total);
        }
        var amostra = new AmostraDeTempos(cadastros.stream().map(Cronometrado::nanos).toList());
        var status = cadastros.stream().map(Cronometrado::resultado)
                .collect(Collectors.groupingBy(s -> s, TreeMap::new, Collectors.counting()));
        var porSegundo = total / (amostra.total() / MILISSEGUNDOS_POR_SEGUNDO);
        var resumo = amostra.resumo()
                + String.format(PORTUGUES, " | %,.0f cadastros/s | total %.1f s | status %s",
                        porSegundo, amostra.total() / MILISSEGUNDOS_POR_SEGUNDO, status);
        return List.of(new Medida("cadastro",
                "cadastro de " + total + " consumidores com um cartão de cada tipo, um por vez",
                resumo, OptionalDouble.of(amostra.mediana())));
    }
}
