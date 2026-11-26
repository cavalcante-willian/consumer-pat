package br.com.alelo.consumer.consumerpat.medicao;

import br.com.alelo.consumer.consumerpat.fabricas.DisputaSimultanea;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Vinte clientes disputam o mesmo cartão em HTTP de verdade, em cinco rodadas, cada uma num
 * consumidor novo com saldo de mil reais e vinte compras de dez.
 *
 * <p>
 * A invariante é a mesma nos dois sistemas: o saldo final tem de ser oitocentos. O legado lê o
 * saldo, subtrai em memória e grava, sem trava, e por isso perde atualizações e termina acima
 * disso. O novo trava o cartão, e termina exato. O tempo das vinte compras mostra o que a trava
 * custa. Roda antes da semeadura, porque o legado só devolve o saldo pela listagem da tabela
 * inteira.
 */
public final class CenarioDeConcorrencia implements Cenario {

    /** Quantas rodadas de disputa. */
    public static final int RODADAS = 5;

    /** Quantas compras disputam em cada rodada. */
    public static final int COMPRAS_POR_RODADA = 20;

    private static final String SALDO_INICIAL = "1000.00";

    private static final String VALOR_DA_COMPRA = "10.00";

    private static final BigDecimal SALDO_ESPERADO = new BigDecimal(SALDO_INICIAL)
            .subtract(new BigDecimal(VALOR_DA_COMPRA).multiply(new BigDecimal(COMPRAS_POR_RODADA)));

    private static final Locale PORTUGUES = Locale.forLanguageTag("pt-BR");

    private static final double MILISSEGUNDOS_POR_SEGUNDO = 1_000.0;

    @Override
    public List<Medida> medir(OperacoesDoAlvo alvo) throws Exception {
        var tempos = new ArrayList<Long>();
        var saldos = new ArrayList<BigDecimal>();
        var status = new ArrayList<Integer>();
        for (int rodada = 0; rodada < RODADAS; rodada++) {
            var cartao = alvo.prepararFarmaciaComSaldo(SALDO_INICIAL);
            List<Callable<Integer>> compras = IntStream.range(0, COMPRAS_POR_RODADA)
                    .<Callable<Integer>>mapToObj(
                            i -> () -> alvo.comprarNaFarmacia(cartao, VALOR_DA_COMPRA))
                    .toList();
            var disputa = MedidorDeTempo.cronometrar(() -> DisputaSimultanea.disputar(compras));
            tempos.add(disputa.nanos());
            status.addAll(disputa.resultado());
            saldos.add(alvo.saldoDe(cartao));
        }
        return List.of(saldoFinal(saldos, status), tempo(tempos));
    }

    private static Medida saldoFinal(List<BigDecimal> saldos, List<Integer> status) {
        var exatos = saldos.stream().filter(saldo -> saldo.compareTo(SALDO_ESPERADO) == 0).count();
        var respostas = status.stream()
                .collect(Collectors.groupingBy(s -> s, TreeMap::new, Collectors.counting()));
        var resumo = String.format(PORTUGUES,
                "saldo final por rodada %s | exato (%s) em %d de %d rodadas | status %s",
                saldos.stream().map(saldo -> saldo.setScale(2, RoundingMode.HALF_EVEN)
                        .toPlainString()).toList(),
                SALDO_ESPERADO.toPlainString(), exatos, RODADAS, respostas);
        return new Medida("concorrencia-saldo-final",
                COMPRAS_POR_RODADA + " compras simultâneas de " + VALOR_DA_COMPRA
                        + " num cartão de " + SALDO_INICIAL + ", em " + RODADAS + " rodadas",
                resumo, OptionalDouble.empty());
    }

    private static Medida tempo(List<Long> tempos) {
        var amostra = new AmostraDeTempos(tempos);
        var porSegundo = COMPRAS_POR_RODADA / (amostra.mediana() / MILISSEGUNDOS_POR_SEGUNDO);
        var resumo = amostra.resumo()
                + String.format(PORTUGUES, " | %,.0f compras/s na mediana", porSegundo);
        return new Medida("concorrencia-tempo",
                "tempo para as " + COMPRAS_POR_RODADA + " compras simultâneas terminarem", resumo,
                OptionalDouble.of(amostra.mediana()));
    }
}
