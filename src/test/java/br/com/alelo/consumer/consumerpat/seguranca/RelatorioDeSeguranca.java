package br.com.alelo.consumer.consumerpat.seguranca;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Junta os achados do legado e do novo, verificação por verificação, numa tabela de texto.
 *
 * <p>
 * Cada verificação vira um bloco com a linha do legado e a do novo, cada uma com o veredito e o que
 * foi observado. No fim, a contagem de verificações em que cada sistema se protegeu. Uma
 * verificação que um dos lados não fez aparece com um traço, e não é inventada. A ordem é a do
 * sistema novo.
 */
public final class RelatorioDeSeguranca {

    private static final String SEM_ACHADO = "—";

    private static final Map<Boolean, String> VEREDITO = Map.of(true, "PROTEGIDO", false,
            "EXPOSTO  ");

    private RelatorioDeSeguranca() {
    }

    /**
     * Monta as linhas do relatório.
     *
     * @param legado os achados do legado
     * @param novo   os achados do novo
     * @return as linhas, na ordem das verificações
     */
    public static List<String> montar(List<Achado> legado, List<Achado> novo) {
        var doLegado = indexar(legado);
        var doNovo = indexar(novo);
        var chaves = new LinkedHashSet<String>();
        novo.forEach(achado -> chaves.add(achado.chave()));
        legado.forEach(achado -> chaves.add(achado.chave()));
        var linhas = new ArrayList<String>();
        for (var chave : chaves) {
            var descricao = Optional.ofNullable(doNovo.get(chave))
                    .or(() -> Optional.ofNullable(doLegado.get(chave))).map(Achado::descricao)
                    .orElse(SEM_ACHADO);
            linhas.add(chave + " — " + descricao);
            linhas.add("  legado: " + texto(doLegado.get(chave)));
            linhas.add("  novo  : " + texto(doNovo.get(chave)));
            linhas.add("");
        }
        linhas.add("Protegido em " + protegidos(legado) + " de " + legado.size()
                + " verificações no legado, e em " + protegidos(novo) + " de " + novo.size()
                + " no novo.");
        return linhas;
    }

    private static Map<String, Achado> indexar(List<Achado> achados) {
        return achados.stream().collect(Collectors.toMap(Achado::chave, Function.identity()));
    }

    private static long protegidos(List<Achado> achados) {
        return achados.stream().filter(Achado::protegido).count();
    }

    private static String texto(Achado achado) {
        return Optional.ofNullable(achado)
                .map(a -> VEREDITO.get(a.protegido()) + " — " + a.evidencia()).orElse(SEM_ACHADO);
    }
}
