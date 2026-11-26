package br.com.alelo.consumer.consumerpat.medicao;

import br.com.alelo.consumer.consumerpat.dominio.cartao.TipoCartao;
import br.com.alelo.consumer.consumerpat.fabricas.JsonDeTeste;

/**
 * Monta o JSON que o legado aceita no cadastro, e numera os consumidores e os cartões dele.
 *
 * <p>
 * O legado guarda documento e número de cartão como {@code int}, de modo que a numeração cabe em
 * pouco mais de dois bilhões: cada consumidor ocupa três números seguidos, um por tipo de cartão, a
 * partir de {@value #BASE_DOS_CARTOES}. Os consumidores da disputa simultânea usam outra faixa,
 * para nunca colidirem com os da base.
 */
public final class CorposDoLegado {

    private static final int BASE_DOS_CARTOES = 100_000_000;

    private static final int BASE_DOS_DOCUMENTOS = 1_000_000;

    private static final int TIPOS = TipoCartao.values().length;

    private CorposDoLegado() {
    }

    /**
     * O documento do consumidor de um índice da base.
     *
     * @param indice o índice
     * @return o documento
     */
    public static int documento(int indice) {
        return BASE_DOS_DOCUMENTOS + indice;
    }

    /**
     * O número do cartão de um tipo, do consumidor de um índice da base.
     *
     * @param indice o índice do consumidor
     * @param tipo   o tipo do cartão
     * @return o número, único na base
     */
    public static int numeroDoCartao(int indice, TipoCartao tipo) {
        return BASE_DOS_CARTOES + indice * TIPOS + tipo.ordinal();
    }

    /**
     * O corpo do cadastro de um consumidor da base, com os três cartões de saldo zero.
     *
     * @param indice o índice do consumidor
     * @return o JSON do cadastro
     */
    public static String cadastro(int indice) {
        return cadastro(documento(indice), numeroDoCartao(indice, TipoCartao.ALIMENTACAO),
                numeroDoCartao(indice, TipoCartao.COMBUSTIVEL),
                numeroDoCartao(indice, TipoCartao.FARMACIA), "0.0");
    }

    /**
     * O corpo do cadastro de um consumidor com saldo só no cartão de farmácia, para a disputa.
     *
     * @param documento o documento
     * @param farmacia  o número do cartão de farmácia
     * @param saldo     o saldo do cartão de farmácia, como decimal com ponto
     * @return o JSON do cadastro
     */
    public static String cadastroDeFarmacia(int documento, int farmacia, String saldo) {
        return cadastro(documento, 0, 0, farmacia, saldo);
    }

    /**
     * O corpo do cadastro de um consumidor com o nome escolhido, que pode ser hostil.
     *
     * @param nome      o nome, qualquer texto: aspas, barras e caracteres de controle vão escapados
     * @param documento o documento
     * @param farmacia  o número do cartão de farmácia
     * @return o JSON do cadastro
     */
    public static String cadastroComNome(String nome, int documento, int farmacia) {
        return """
                {"name":"%s","documentNumber":%d,"drugstoreNumber":%d}"""
                .formatted(JsonDeTeste.escapar(nome), documento, farmacia);
    }

    /**
     * O corpo da atualização do cadastro de um consumidor que já existe, mandando o saldo.
     *
     * @param id       o id do consumidor
     * @param farmacia o número do cartão de farmácia
     * @param saldo    o saldo que o atacante quer impor, como decimal com ponto
     * @return o JSON da atualização
     */
    public static String atualizacaoComSaldo(int id, int farmacia, String saldo) {
        return """
                {"id":%d,"name":"Atualizado","drugstoreNumber":%d,"drugstoreCardBalance":%s}"""
                .formatted(id, farmacia, saldo);
    }

    private static String cadastro(int documento, int alimentacao, int combustivel, int farmacia,
            String saldoDaFarmacia) {
        return """
                {"name":"Consumidor de medição","documentNumber":%d,\
                "email":"consumidor@exemplo.com.br","street":"Rua A","number":1,\
                "city":"Recife","country":"Brasil","portalCode":50000000,\
                "foodCardNumber":%d,"foodCardBalance":0.0,\
                "fuelCardNumber":%d,"fuelCardBalance":0.0,\
                "drugstoreNumber":%d,"drugstoreCardBalance":%s}""".formatted(documento, alimentacao,
                combustivel, farmacia, saldoDaFarmacia);
    }
}
