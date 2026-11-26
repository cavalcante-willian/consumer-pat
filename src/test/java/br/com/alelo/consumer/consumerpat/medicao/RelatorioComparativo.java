package br.com.alelo.consumer.consumerpat.medicao;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Junta as medidas do legado e do novo, chamada por chamada, numa tabela de texto.
 *
 * <p>
 * Cada chamada vira um bloco com a linha do legado, a do novo e a razão entre as medianas. A razão
 * é o tempo do legado dividido pelo do novo: acima de um, o novo é mais rápido, e abaixo de um, o
 * novo é mais lento, como no custo da trava da disputa. Uma chamada que um sistema não tem aparece
 * com um traço, e não é inventada. A ordem dos blocos é a do sistema novo, que tem todas as
 * chamadas, na ordem dos cenários; as que só o legado tem vêm depois.
 */
public final class RelatorioComparativo {

    private static final Locale PORTUGUES = Locale.forLanguageTag("pt-BR");

    private static final String SEM_MEDIDA = "—";

    private RelatorioComparativo() {
    }

    /**
     * Monta as linhas do relatório.
     *
     * @param legado as medidas do legado
     * @param novo   as medidas do novo
     * @return as linhas, na ordem em que as chamadas apareceram
     */
    public static List<String> montar(List<Medida> legado, List<Medida> novo) {
        var doLegado = indexar(legado);
        var doNovo = indexar(novo);
        var chaves = new LinkedHashSet<String>();
        novo.forEach(medida -> chaves.add(medida.chave()));
        legado.forEach(medida -> chaves.add(medida.chave()));
        var linhas = new ArrayList<String>();
        for (var chave : chaves) {
            linhas.add(chave);
            linhas.add("  legado: " + texto(doLegado.get(chave)));
            linhas.add("  novo  : " + texto(doNovo.get(chave)));
            linhas.add("  razão : " + razao(doLegado.get(chave), doNovo.get(chave)));
            linhas.add("");
        }
        return linhas;
    }

    private static Map<String, Medida> indexar(List<Medida> medidas) {
        return medidas.stream().collect(Collectors.toMap(Medida::chave, Function.identity()));
    }

    private static String texto(Medida medida) {
        return Optional.ofNullable(medida).map(m -> m.descricao() + "\n          " + m.resumo())
                .orElse(SEM_MEDIDA);
    }

    private static Optional<Double> principal(Medida medida) {
        return Optional.ofNullable(medida).map(Medida::principal).filter(OptionalDouble::isPresent)
                .map(OptionalDouble::getAsDouble);
    }

    private static String razao(Medida legado, Medida novo) {
        return principal(legado).flatMap(l -> principal(novo)
                .map(n -> String.format(PORTUGUES, "%.1f× (legado ÷ novo, pela mediana)", l / n)))
                .orElse(SEM_MEDIDA);
    }
}
