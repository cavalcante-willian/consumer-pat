package br.com.alelo.consumer.consumerpat.medicao;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Mede uma lista de pedidos do jeito de sempre: três execuções de aquecimento descartadas, dez
 * medidas, e a mediana como o número que se compara.
 *
 * <p>
 * Além do tempo, a medida registra o tamanho da resposta e a contagem dos status, porque um tempo
 * sem o status engana: uma resposta rápida de {@code 404} ou de {@code 422} não é uma compra.
 */
public final class MedicaoDePedidos {

    /** Quantas execuções descartar antes de medir. */
    public static final int AQUECIMENTO = 3;

    /** Quantas execuções medir. */
    public static final int MEDICOES = 10;

    private static final Locale PORTUGUES = Locale.forLanguageTag("pt-BR");

    private MedicaoDePedidos() {
    }

    /**
     * Mede cada pedido, na ordem.
     *
     * @param pedidos os pedidos
     * @return uma medida por pedido
     * @throws Exception se uma chamada falha
     */
    public static List<Medida> medir(List<Pedido> pedidos) throws Exception {
        var medidas = new ArrayList<Medida>();
        for (var pedido : pedidos) {
            medidas.add(medir(pedido));
        }
        return medidas;
    }

    private static Medida medir(Pedido pedido) throws Exception {
        var medidos = MedidorDeTempo.medir(AQUECIMENTO, MEDICOES,
                () -> ResumoDaResposta.de(pedido.chamada().call()));
        var amostra = new AmostraDeTempos(medidos.stream().map(Cronometrado::nanos).toList());
        var status = medidos.stream().map(medido -> medido.resultado().status())
                .collect(Collectors.groupingBy(s -> s, TreeMap::new, Collectors.counting()));
        var bytes = medidos.get(0).resultado().bytes();
        var resumo = amostra.resumo() + String.format(PORTUGUES, " | resposta %,d bytes", bytes)
                + " | status " + status;
        return new Medida(pedido.chave(), pedido.descricao(), resumo,
                OptionalDouble.of(amostra.mediana()));
    }
}
